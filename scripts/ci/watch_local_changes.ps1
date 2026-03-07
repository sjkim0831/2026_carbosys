Param(
  [int]$IntervalSec = 5,
  [string]$AppContainer = 'carbosys-app',
  [string]$MsaManagerUrl = 'http://localhost:18030/admin/msa'
)

$RootDir = Resolve-Path (Join-Path $PSScriptRoot '..\..')
Set-Location $RootDir

function Get-SnapshotHash {
  $lines = Get-ChildItem -Path $RootDir -Recurse -File |
    Where-Object {
      $_.FullName -notmatch '\\.git\\' -and
      $_.FullName -notmatch '\\logs\\' -and
      $_.FullName -notmatch '\\data\\' -and
      $_.FullName -notmatch '\\file\\' -and
      $_.FullName -notmatch '\\module\\[^\\]+\\target\\' -and
      $_.FullName -notmatch '\\.ops-control\\'
    } |
    Sort-Object FullName |
    ForEach-Object { '{0}|{1}' -f ($_.FullName.Replace('\\','/')), $_.LastWriteTimeUtc.Ticks }

  if (-not $lines) { return '' }
  $joined = ($lines -join "`n")
  $bytes = [System.Text.Encoding]::UTF8.GetBytes($joined)
  $sha = [System.Security.Cryptography.SHA256]::Create()
  try {
    return ([System.BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
  } finally {
    $sha.Dispose()
  }
}

$last = ''
Write-Host "Watching local folder changes at $RootDir (interval=$IntervalSec s)"
while ($true) {
  $cur = Get-SnapshotHash
  if ([string]::IsNullOrEmpty($last)) {
    $last = $cur
  } elseif ($cur -ne $last) {
    $last = $cur
    Write-Host 'Change detected. Running module build/deploy pipeline...'
    $env:APP_CONTAINER = $AppContainer
    $env:MSA_MANAGER_URL = $MsaManagerUrl
    & bash scripts/ci/run_changed_modules_pipeline.sh --source working-tree
  }
  Start-Sleep -Seconds $IntervalSec
}
