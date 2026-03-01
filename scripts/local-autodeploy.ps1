$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

./scripts/ci/watch_local_changes.ps1
