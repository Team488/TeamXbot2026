# fetch-logs.ps1
# Downloads .wpilog files from roboRIO-488-frc.local to a local directory.
# Skips files that already exist locally with the same size (rsync-like behavior).
# Transfers run in parallel to overlap SSH handshake overhead across files.
#
# Usage: .\fetch-logs.ps1 [-Destination <path>] [-Force] [-Throttle <n>]
#   -Destination  Where to save logs (default: .\robot-logs)
#   -Force        Re-download everything, ignoring local copies
#   -Throttle     Max simultaneous transfers (default: 4)

param(
    [string]$Destination = "robot-logs",
    [switch]$Force,
    [int]$Throttle = 4
)

$Robot        = "roboRIO-488-frc.local"
$User         = "admin"
$RemoteLogDir = "/u/logs"   # roboRIO mounts USB at /u

$SshOpts = @(
    "-o", "StrictHostKeyChecking=no",
    "-o", "UserKnownHostsFile=/dev/null",
    "-o", "LogLevel=ERROR",
    "-o", "ConnectTimeout=5",
    # chacha20 is faster than AES on ARM CPUs that lack hardware AES acceleration (like the roboRIO)
    "-o", "Ciphers=chacha20-poly1305@openssh.com,aes128-ctr"
)

New-Item -ItemType Directory -Force -Path $Destination | Out-Null
$Destination = (Resolve-Path $Destination).Path

Write-Host "Connecting to $User@$Robot ..."

# Fetch remote file list as "path size mtime" triples using stat
$RemoteListing = & ssh @SshOpts "$User@$Robot" `
    "find $RemoteLogDir -maxdepth 1 -name '*.wpilog' -exec stat -c '%n %s %Y' {} +" 2>&1

if ($LASTEXITCODE -ne 0 -or -not $RemoteListing) {
    Write-Error "Could not list log files on robot. Is it connected and is a USB key inserted? (checked $RemoteLogDir)"
    exit 1
}

# Parse into objects: { Path, Name, RemoteSize, Mtime }, sorted newest first
$RemoteFiles = $RemoteListing -split "`n" |
    Where-Object { $_ -match "\.wpilog" } |
    ForEach-Object {
        $parts = $_.Trim() -split " "
        [PSCustomObject]@{
            Path       = $parts[0]
            Name       = Split-Path $parts[0] -Leaf
            RemoteSize = [long]$parts[1]
            Mtime      = [long]$parts[2]
        }
    } |
    Sort-Object Mtime -Descending

Write-Host "Found $($RemoteFiles.Count) log file(s) on robot."
Write-Host ""

# Decide what needs downloading
$ToDownload = [System.Collections.Generic.List[PSCustomObject]]::new()
$Skipped = 0

foreach ($f in $RemoteFiles) {
    $LocalPath = Join-Path $Destination $f.Name
    if (-not $Force -and (Test-Path $LocalPath)) {
        $LocalSize = (Get-Item $LocalPath).Length
        if ($LocalSize -eq $f.RemoteSize) {
            Write-Host "  [skip] $($f.Name)  ($($f.RemoteSize) bytes, already synced)"
            $Skipped++
            continue
        }
        Write-Host "  [queue/update] $($f.Name)  (local $LocalSize B -> remote $($f.RemoteSize) B)"
    } else {
        Write-Host "  [queue] $($f.Name)  ($($f.RemoteSize) bytes)"
    }
    $f | Add-Member -NotePropertyName LocalPath -NotePropertyValue $LocalPath
    $ToDownload.Add($f)
}

if ($ToDownload.Count -eq 0) {
    Write-Host ""
    Write-Host "Nothing to download. Skipped: $Skipped"
    exit 0
}

Write-Host ""
Write-Host "Downloading $($ToDownload.Count) file(s) with up to $Throttle parallel transfer(s) ..."
Write-Host ""

$TotalBytes = ($ToDownload | Measure-Object -Property RemoteSize -Sum).Sum
$DoneBytes  = 0
$Copied     = 0
$Failed     = 0
$Queue      = [System.Collections.Generic.Queue[PSCustomObject]]($ToDownload)
$Active     = [System.Collections.Generic.List[object]]::new()

$Stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

# Single unified loop: launch new jobs when slots are free, collect finished ones, update progress.
while ($Queue.Count -gt 0 -or $Active.Count -gt 0) {

    # Launch as many jobs as throttle allows
    while ($Queue.Count -gt 0 -and $Active.Count -lt $Throttle) {
        $f = $Queue.Dequeue()
        $job = Start-Job -ScriptBlock {
            param($User, $Robot, $RemotePath, $LocalPath)
            $opts = @(
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=/dev/null",
                "-o", "LogLevel=ERROR",
                "-o", "ConnectTimeout=5",
                "-o", "Ciphers=chacha20-poly1305@openssh.com,aes128-ctr"
            )
            & scp @opts "${User}@${Robot}:${RemotePath}" "$LocalPath" 2>&1
            return $LASTEXITCODE
        } -ArgumentList $User, $Robot, $f.Path, $f.LocalPath

        $job | Add-Member -NotePropertyName FileName   -NotePropertyValue $f.Name
        $job | Add-Member -NotePropertyName LocalPath  -NotePropertyValue $f.LocalPath
        $job | Add-Member -NotePropertyName RemoteSize -NotePropertyValue $f.RemoteSize
        $Active.Add($job)
    }

    # Collect any finished jobs
    $Done = @($Active | Where-Object { $_.State -ne 'Running' })
    foreach ($job in $Done) {
        $ExitCode = Receive-Job $job
        Remove-Job $job
        $Active.Remove($job) | Out-Null
        if ($ExitCode -eq 0) {
            Write-Host "  [done] $($job.FileName)"
            $DoneBytes += $job.RemoteSize
            $Copied++
        } else {
            Write-Warning "  [fail] $($job.FileName)"
            $Failed++
        }
    }

    # Update progress bar: count bytes written so far for in-flight files
    $InFlightBytes = 0
    $ActiveNames   = @()
    foreach ($job in $Active) {
        if (Test-Path $job.LocalPath) { $InFlightBytes += (Get-Item $job.LocalPath).Length }
        $ActiveNames += $job.FileName
    }

    $ReceivedBytes = $DoneBytes + $InFlightBytes
    $Pct           = if ($TotalBytes -gt 0) { [int]($ReceivedBytes / $TotalBytes * 100) } else { 0 }
    $ElapsedSec    = $Stopwatch.Elapsed.TotalSeconds
    $MBps          = if ($ElapsedSec -gt 0) { [math]::Round($ReceivedBytes / 1MB / $ElapsedSec, 1) } else { 0 }
    $Queued        = $Queue.Count
    $StatusMsg     = "{0:F1} / {1:F1} MB  |  {2} MB/s  |  {3} queued  |  {4}" -f ($ReceivedBytes/1MB), ($TotalBytes/1MB), $MBps, $Queued, ($ActiveNames -join ', ')
    Write-Progress -Id 1 -Activity "Fetching logs" -Status $StatusMsg -PercentComplete $Pct

    Start-Sleep -Milliseconds 300
}

$Stopwatch.Stop()
Write-Progress -Id 1 -Activity "Fetching logs" -Completed

$Elapsed = $Stopwatch.Elapsed
$TotalBytes = ($ToDownload | Measure-Object -Property RemoteSize -Sum).Sum
$MBps = if ($Elapsed.TotalSeconds -gt 0) { [math]::Round($TotalBytes / 1MB / $Elapsed.TotalSeconds, 2) } else { "?" }

Write-Host ""
Write-Host "Done in $($Elapsed.ToString('mm\:ss\.ff')).  Copied: $Copied  Skipped: $Skipped  Failed: $Failed"
Write-Host "Transfer rate: $MBps MB/s  ($([math]::Round($TotalBytes / 1MB, 1)) MB total)"
Write-Host "Logs saved to: $Destination"
