$ErrorActionPreference = Stop
$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

bash ./scripts/local-autodeploy.sh
