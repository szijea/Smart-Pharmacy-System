$base = 'http://localhost:8080'
$dir = 'e2e_results'
if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

function Save-Resp($name, $resp) {
    $path = Join-Path $dir ($name + '.txt')
    if ($resp -is [string]) {
        $resp | Out-File -FilePath $path -Encoding utf8
    } else {
        try {
            $json = $resp | ConvertTo-Json -Depth 10
            $json | Out-File -FilePath $path -Encoding utf8
        } catch {
            $resp | Out-File -FilePath $path -Encoding utf8
        }
    }
}

function Do-Req($step, $scriptblock) {
    Write-Host "Running $step..."
    try {
        $result = & $scriptblock
        Save-Resp $step $result
        Write-Host "$step succeeded"
    } catch {
        $err = $_.Exception.Message
        Save-Resp ($step + '_error') $err
        if ($_.Exception.Response) {
            try { $c = $_.Exception.Response.Content.ReadAsStringAsync().Result; Save-Resp ($step + '_error_body') $c } catch {}
        }
        Write-Host "$step failed: $err"
    }
}

# 1. Health (use Invoke-WebRequest to capture status)
Do-Req '01_health' { Invoke-WebRequest -Uri "$base/api/medicines/health" -Method Get }

# 2. Create med1
$med1File = "e2e_med1.json"
Do-Req '02_create_med1' { Invoke-WebRequest -Uri "$base/api/medicines" -Method Post -ContentType 'application/json' -InFile $med1File }

# 3. Create med1 again (duplicate)
Do-Req '03_create_med1_dup' { Invoke-WebRequest -Uri "$base/api/medicines" -Method Post -ContentType 'application/json' -InFile $med1File }

# 4. Create med2
$med2File = "e2e_med2.json"
Do-Req '04_create_med2' { Invoke-WebRequest -Uri "$base/api/medicines" -Method Post -ContentType 'application/json' -InFile $med2File }

# 5. Post stock-in
$stockFile = "e2e_stock.json"
Do-Req '05_create_stockin' { Invoke-WebRequest -Uri "$base/api/stock-ins" -Method Post -ContentType 'application/json' -InFile $stockFile }

# 6. Search aggregated
Do-Req '06_search' { Invoke-WebRequest -Uri "$base/api/medicines/search?keyword=E2E" -Method Get }

# 7. List all medicines (page 0 size 100)
Do-Req '07_list_meds' { Invoke-WebRequest -Uri "$base/api/medicines?page=0&size=100" -Method Get }

Write-Host 'E2E script finished. Results are in e2e_results directory.'

