<#
interactive_fix.ps1
Interactive fix script for medicine_id type/collation inconsistencies across wx, bht, rzt_db tenants.
Will prompt for MySQL password multiple times. Run in test environment first.
#>

# Config
$tenants = @('wx','bht','rzt_db')
$generateSql = Join-Path $PSScriptRoot 'generate_drop_fks.sql'
$dropCandidates = Join-Path $PSScriptRoot 'drop_fks_candidates.sql'

Write-Host "Interactive Fix Script for medicine_id inconsistencies" -ForegroundColor Cyan

# Get MySQL username
$dbUser = Read-Host "Enter MySQL username (default: root)"; if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'root' }
Write-Host "Using MySQL user: $dbUser" -ForegroundColor Yellow

# Show plan
Write-Host "Script will:" -ForegroundColor Green
Write-Host " 1) Generate DROP FK candidates" -ForegroundColor Green
Write-Host " 2) Optionally execute DROP FK" -ForegroundColor Green
Write-Host " 3) Backup databases (optional)" -ForegroundColor Green
Write-Host " 4) Run fix scripts per tenant" -ForegroundColor Green
Write-Host " 5) Run verification" -ForegroundColor Green

$ok = Read-Host "Continue? (Y/N)"; if ($ok -ne 'Y' -and $ok -ne 'y') { Write-Host 'Cancelled'; exit }

# Step 1: Generate DROP FK candidates
if (-not (Test-Path $generateSql)) {
    Write-Host "Cannot find $generateSql in db/fix-scripts/" -ForegroundColor Red; exit
}

Write-Host "Generating DROP FK candidates..." -ForegroundColor Cyan
$sqlContent = Get-Content $generateSql -Raw
$sqlContent | & mysql "-u$dbUser" -p -B | Out-File $dropCandidates -Encoding utf8
Write-Host "Generated: $dropCandidates" -ForegroundColor Green

Write-Host "Preview (first 200 lines):" -ForegroundColor Yellow
Get-Content $dropCandidates -TotalCount 200 | ForEach-Object { Write-Host $_ }

# Ask to execute DROP
$review = Read-Host "Execute DROP FK? (DROP/SKIP)";
if ($review -eq 'DROP') {
    Write-Host "Executing DROP FK statements..." -ForegroundColor Yellow
    Get-Content $dropCandidates | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | & mysql "-u$dbUser" -p
    Write-Host "DROP FK complete. Check output for errors." -ForegroundColor Green
} else {
    Write-Host "Skipped DROP FK." -ForegroundColor Yellow
}

# Step 2: Backup
$bkOption = Read-Host "Backup databases now? (Y/N)";
if ($bkOption -eq 'Y' -or $bkOption -eq 'y') {
    foreach ($db in $tenants) {
        $ts = Get-Date -Format yyyyMMddHHmmss
        $file = Join-Path $PSScriptRoot "${db}_backup_${ts}.sql"
        Write-Host "Backing up $db to $file ..." -ForegroundColor Cyan
        & mysqldump "-u$dbUser" -p --single-transaction --set-gtid-purged=OFF $db | Out-File $file -Encoding utf8
        Write-Host "Backup complete: $file" -ForegroundColor Green
    }
} else {
    Write-Host "Skipped backup. Ensure you have manual backups!" -ForegroundColor Yellow
}

# Step 3: Run fix scripts per tenant
$tenantScripts = @{ 'wx' = 'wx_fix.sql'; 'bht' = 'bht_fix.sql'; 'rzt_db' = 'rzt_db_fix.sql' }
foreach ($db in $tenants) {
    $scriptFile = Join-Path $PSScriptRoot $tenantScripts[$db]
    if (-not (Test-Path $scriptFile)) { Write-Host "Script not found: $scriptFile, skipping $db" -ForegroundColor Yellow; continue }
    Write-Host "Ready to run fix script for $db : $scriptFile" -ForegroundColor Cyan
    $confirm = Read-Host "Execute $scriptFile now? (Y/N)"
    if ($confirm -ne 'Y' -and $confirm -ne 'y') { Write-Host "Skipped $db" -ForegroundColor Yellow; continue }
    Write-Host "Executing $scriptFile (will prompt for password)..." -ForegroundColor Cyan
    # Use pipeline instead of < redirection
    $scriptContent = Get-Content $scriptFile -Raw
    $scriptContent | & mysql "-u$dbUser" -p
    Write-Host "Executed $scriptFile. Check output for errors." -ForegroundColor Green
}

# Step 4: Verification
Write-Host "Running verification queries..." -ForegroundColor Cyan
$verify1 = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, CHARACTER_SET_NAME, COLLATION_NAME FROM information_schema.COLUMNS WHERE COLUMN_NAME='medicine_id' AND TABLE_SCHEMA IN ('wx','bht','rzt_db') ORDER BY TABLE_SCHEMA, TABLE_NAME;"
$verify2 = "SELECT TABLE_SCHEMA, TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_NAME='medicine' AND REFERENCED_COLUMN_NAME='medicine_id' AND TABLE_SCHEMA IN ('wx','bht','rzt_db') ORDER BY TABLE_SCHEMA, TABLE_NAME;"

& mysql "-u$dbUser" -p -e $verify1 | Out-File .\db\fix-scripts\verify_columns.txt -Encoding utf8
& mysql "-u$dbUser" -p -e $verify2 | Out-File .\db\fix-scripts\verify_fks.txt -Encoding utf8

Write-Host "Verification output written to db/fix-scripts/verify_*.txt" -ForegroundColor Green
Get-Content .\db\fix-scripts\verify_columns.txt | ForEach-Object { Write-Host $_ }
Get-Content .\db\fix-scripts\verify_fks.txt | ForEach-Object { Write-Host $_ }

Write-Host "Interactive fix script complete. Paste verify files back if you need further assistance." -ForegroundColor Cyan

