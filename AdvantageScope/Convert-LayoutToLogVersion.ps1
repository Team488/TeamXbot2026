param (
    [string]$inputFilePath = ".\SimulatorLayout.json",
    [string]$outputFilePath = ".\LoggedLayout.json"
)

$pathToReplace = "NT:/AdvantageKit/"

if (-Not (Test-Path -Path $inputFilePath)) {
    Write-Host "Input file '$inputFilePath' does not exist. Please provide a valid file path."
    exit 1
}

# Read the content of the input file
$content = Get-Content -Path $inputFilePath -Raw

# Sanity check to ensure the content contains the expected string before performing replacements
if ($content -like "*$pathToReplace*") {
    Write-Host "Found '$pathToReplace' in the content. Proceeding with replacements."
} else {
    Write-Host "No instances of '$pathToReplace' found in the content. No replacements needed."
    exit
}

# Replace all instances of the path with a blank string
$modifiedContent = $content -replace $pathToReplace, "/"

# Write the modified content to the output file
Set-Content -Path $outputFilePath -Value $modifiedContent

Write-Host "Replacements complete. Modified content saved to $outputFilePath"