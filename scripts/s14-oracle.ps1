param([Parameter(Mandatory=$true)][string]$SqlFile)
$ErrorActionPreference='Stop'
$config=Get-Content "$PSScriptRoot/../src/main/resources/application-dev.yaml" -Raw
$dbPassword=([regex]::Match($config,'password: (.+)').Groups[1].Value.Trim())
$env:S14_DB_CONNECT="pharmaDB/$dbPassword@//localhost:1521/FREEPDB1"
$sql=Get-Content -LiteralPath $SqlFile -Raw
try {
  $sql | docker exec -i -e S14_DB_CONNECT oracle-local sh -c 'sqlplus -s "$S14_DB_CONNECT"'
  if($LASTEXITCODE -ne 0){throw 'SQLPlus devolvió un error'}
} finally {Remove-Item Env:S14_DB_CONNECT}
