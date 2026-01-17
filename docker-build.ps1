Set-Location -Path "$PSScriptRoot"
Write-Host "Building Docker image..."
docker build -t pharmacy-system:v1.2.3 .
if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful. Exporting to tar..."
    docker save -o pharmacy-system.tar pharmacy-system:v1.2.3
    Write-Host "Done! File saved to $(Get-Location)\pharmacy-system.tar"
} else {
    Write-Host "Build failed. Please check your network connection (Docker Hub access)." -ForegroundColor Red
}
