param([string]$Ttl='10m')
$ErrorActionPreference='Stop'
$env:SPRING_PROFILES_ACTIVE='prod'
$env:DB_URL='jdbc:oracle:thin:@//localhost:1522/FREEPDB1'
$env:DB_USERNAME='pharmaDB'
$config=Get-Content "$PSScriptRoot/../src/main/resources/application-dev.yaml" -Raw
$env:DB_PASSWORD=([regex]::Match($config,'password: (.+)').Groups[1].Value.Trim())
$bytes=New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET=[Convert]::ToBase64String($bytes)
$p=Start-Process -FilePath 'C:/Users/Asus/.jdks/ms-21.0.12.1/bin/java.exe' -ArgumentList @('-jar','target/PharmaBackckend-0.0.1-SNAPSHOT.jar',"--spring.cache.redis.time-to-live=$Ttl") -WindowStyle Hidden -PassThru -RedirectStandardOutput "evidence/s13/autonoma-$Ttl.log" -RedirectStandardError "evidence/s13/autonoma-$Ttl-error.log"
$p.Id | Set-Content evidence/s13/backend-prod.pid
Write-Output "Backend iniciado PID=$($p.Id), TTL=$Ttl"
