$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

docker compose --profile ci up -d --build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Jenkins + app stack is up."
Write-Host "Jenkins: http://localhost:18081"
