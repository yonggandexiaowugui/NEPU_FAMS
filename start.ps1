# ====================================
# NEPU_FAMS 一键启动脚本
# ====================================

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  NEPU_FAMS 固定资产管理系统 - 启动中..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 项目根目录
$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path

# ====================================
# 1. 检查端口占用
# ====================================
$BACKEND_PORT = 8088
$FRONTEND_PORT = 5173

function Check-Port($port) {
    $proc = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    if ($proc) {
        Write-Host "[端口] Port $port 已被占用 (PID: $proc)，正在停止..." -ForegroundColor Yellow
        taskkill /F /PID $proc > $null
        Start-Sleep -Seconds 1
        Write-Host "[端口] Port $port 已释放" -ForegroundColor Green
    }
}

Check-Port $BACKEND_PORT
Check-Port $FRONTEND_PORT

# ====================================
# 2. 启动后端 (Spring Boot)
# ====================================
Write-Host "[后端] 正在启动 Spring Boot (Port $BACKEND_PORT)..." -ForegroundColor Gray
$backendJob = Start-Job -Name "NEPU_FAMS-Backend" -ScriptBlock {
    param($dir)
    Set-Location $dir
    .\mvnw.cmd spring-boot:run -q
} -ArgumentList $PROJECT_DIR

# 等几秒让后端先启动一些
Start-Sleep -Seconds 3

# ====================================
# 3. 启动前端 (Vite)
# ====================================
Write-Host "[前端] 正在启动 Vite (Port $FRONTEND_PORT)..." -ForegroundColor Gray
$frontendJob = Start-Job -Name "NEPU_FAMS-Frontend" -ScriptBlock {
    param($dir)
    Set-Location "$dir\frontend"
    npm run dev
} -ArgumentList $PROJECT_DIR

# ====================================
# 4. 等待后端启动完成
# ====================================
Write-Host "" -NoNewline
for ($i = 0; $i -lt 15; $i++) {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:$BACKEND_PORT/api/statistics/overview" -UseBasicParsing -TimeoutSec 2
        if ($r.StatusCode -eq 200) {
            Write-Host ""
            Write-Host "[后端] 启动成功！" -ForegroundColor Green
            break
        }
    } catch {
        # 还在启动中
    }
    Write-Host "." -NoNewline -ForegroundColor Gray
    Start-Sleep -Seconds 1
}
Write-Host ""

# ====================================
# 5. 等待前端启动完成
# ====================================
Start-Sleep -Seconds 8

# ====================================
# 6. 输出结果
# ====================================
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  启动完成！" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  前端地址: http://localhost:$FRONTEND_PORT" -ForegroundColor Green
Write-Host "  后端地址: http://localhost:$BACKEND_PORT" -ForegroundColor Green
Write-Host ""
Write-Host "  测试账号:" -ForegroundColor Yellow
Write-Host "    admin / admin123     (超级管理员)" -ForegroundColor Gray
Write-Host "    college_admin1 / 123456  (学院管理员)" -ForegroundColor Gray
Write-Host "    user1 / 123456      (普通用户)" -ForegroundColor Gray
Write-Host ""
Write-Host "  停止服务: 关闭此窗口即可" -ForegroundColor Gray
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 保持窗口打开，直到用户关闭
try {
    while ($true) {
        # 检查前后端是否还在运行
        $be = Get-Job -Name "NEPU_FAMS-Backend" -ErrorAction SilentlyContinue
        $fe = Get-Job -Name "NEPU_FAMS-Frontend" -ErrorAction SilentlyContinue
        if (-not $be -or $be.State -eq "Failed") {
            Write-Host "[后端] 服务已停止" -ForegroundColor Red
        }
        if (-not $fe -or $fe.State -eq "Failed") {
            Write-Host "[前端] 服务已停止" -ForegroundColor Red
        }
        Start-Sleep -Seconds 10
    }
} finally {
    Stop-Job -Name "NEPU_FAMS-Backend" -ErrorAction SilentlyContinue
    Stop-Job -Name "NEPU_FAMS-Frontend" -ErrorAction SilentlyContinue
    Remove-Job -Name "NEPU_FAMS-Backend" -ErrorAction SilentlyContinue
    Remove-Job -Name "NEPU_FAMS-Frontend" -ErrorAction SilentlyContinue
}
