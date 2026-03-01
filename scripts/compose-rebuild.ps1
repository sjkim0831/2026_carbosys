Param(
  [string]$Profile = 'app'
)

$ErrorActionPreference = Stop
$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

if ($Profile -eq 'ci') {
  docker compose --profile ci up -d --build
} else {
  docker compose up -d --build
}

Write-Host "compose rebuild up completed (profile=$Profile)"
