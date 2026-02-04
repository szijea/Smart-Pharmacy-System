#!/usr/bin/env powershell
# 快速修复脚本：直接为所有数据库的 order_item 表添加 id 列

param(
    [string]$Host = 'localhost',
    [int]$Port = 3306,
    [string]$User = 'root',
    [string]$Password = '123456'  # 根据 application.yaml 配置
)

$databases = @('wx', 'bht', 'rzt_db')
$FixScriptPath = Join-Path $PSScriptRoot 'fix_all_order_item_tables.sql'

Write-Host "========================================" -ForegroundColor Green
Write-Host "order_item 表 ID 列快速修复脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

if (-not (Test-Path $FixScriptPath)) {
    Write-Host "错误: 找不到修复脚本" -ForegroundColor Red
    exit 1
}

Write-Host "`n执行修复SQL..." -ForegroundColor Cyan

try {
    $cmd = @(
        "-h$Host",
        "-P$Port",
        "-u$User",
        "-p$Password"
    )
    
    Get-Content $FixScriptPath | mysql @cmd
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✓ 修复成功！" -ForegroundColor Green
        Write-Host "`n接下来请：" -ForegroundColor Yellow
        Write-Host "1. 重启 Spring Boot 应用" -ForegroundColor Yellow
        Write-Host "2. 再次尝试创建订单或查看订单详情" -ForegroundColor Yellow
    } else {
        Write-Host "`n✗ 修复失败 (exit code: $LASTEXITCODE)" -ForegroundColor Red
    }
} catch {
    Write-Host "`n✗ 执行异常: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
