#!/usr/bin/env powershell
# 修复脚本：为所有租户数据库添加缺失的 order_item.id 列

param(
    [string]$Host = 'localhost',
    [int]$Port = 3306,
    [string]$User = 'root',
    [string]$Password,
    [string[]]$Databases = @('wx', 'bht', 'rzt_db')
)

# 如果没有提供密码，提示输入
if (-not $Password) {
    $Password = Read-Host "请输入 MySQL 密码" -AsSecureString
    $Password = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUni($Password))
}

$FixScriptPath = Join-Path $PSScriptRoot 'fix_order_item_id_column.sql'

if (-not (Test-Path $FixScriptPath)) {
    Write-Host "错误：找不到修复脚本 $FixScriptPath" -ForegroundColor Red
    exit 1
}

Write-Host "开始修复 order_item 表 id 列问题..." -ForegroundColor Green

foreach ($db in $Databases) {
    Write-Host "`n正在处理数据库: $db" -ForegroundColor Cyan
    
    try {
        # 构建 mysql 命令
        $mysqlCmd = @(
            "-h", $Host,
            "-P", $Port,
            "-u", $User,
            "-p$Password",
            $db
        )
        
        # 执行修复脚本
        Get-Content $FixScriptPath | mysql @mysqlCmd
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ $db 修复成功" -ForegroundColor Green
        } else {
            Write-Host "✗ $db 修复失败 (exit code: $LASTEXITCODE)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $db 修复异常: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n修复完成！" -ForegroundColor Green
