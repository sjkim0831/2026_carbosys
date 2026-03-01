Param(
  [ValidateSet('app','ci')]
  [string]$Profile = 'app',
  [switch]$Build
)

$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

$cmd = @('compose')
if ($Profile -eq 'ci') {
  $cmd += @('--profile', 'ci')
}
$cmd += @('up', '-d')
if ($Build.IsPresent) {
  $cmd += '--build'
}

& docker $cmd
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "compose up completed (profile=$Profile)"
