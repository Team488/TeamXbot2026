# fetch-logs.ps1
# Downloads .wpilog files from roboRIO-488-frc.local to a local directory.
# Skips files that already exist locally with the same size (rsync-like behavior).
#
# Usage: .\fetch-logs.ps1 [-Destination <path>] [-Force]
#   -Destination  Where to save logs (default: .\robot-logs)
#   -Force        Re-download everything, ignoring local copies

param(
    [string]$Destination = "robot-logs",
    [switch]$Force
)

$Robot        = "roboRIO-488-frc.local"
$User         = "admin"
$RemoteLogDir = "/u/logs"   # roboRIO mounts USB at /u

$SshOpts = @(
    "-o", "StrictHostKeyChecking=no",
    "-o", "UserKnownHostsFile=/dev/null",
    "-o", "LogLevel=ERROR",
    "-o", "ConnectTimeout=5"
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

$Copied  = 0
$Skipped = 0
$Failed  = 0

foreach ($f in $RemoteFiles) {
    $LocalPath = Join-Path $Destination $f.Name

    if (-not $Force -and (Test-Path $LocalPath)) {
        $LocalSize = (Get-Item $LocalPath).Length
        if ($LocalSize -eq $f.RemoteSize) {
            Write-Host "  [skip] $($f.Name)  ($($f.RemoteSize) bytes, already synced)"
            $Skipped++
            continue
        }
        Write-Host "  [update] $($f.Name)  (local $LocalSize B → remote $($f.RemoteSize) B)"
    } else {
        Write-Host "  [copy] $($f.Name)  ($($f.RemoteSize) bytes)"
    }

    & scp @SshOpts "${User}@${Robot}:$($f.Path)" "$LocalPath"
    if ($LASTEXITCODE -eq 0) {
        $Copied++
    } else {
        Write-Warning "  Failed to copy $($f.Name)"
        $Failed++
    }
}

Write-Host ""
Write-Host "Done.  Copied: $Copied  Updated: (see above)  Skipped: $Skipped  Failed: $Failed"
Write-Host "Logs saved to: $Destination"
