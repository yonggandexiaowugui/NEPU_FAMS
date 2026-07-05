@echo off
chcp 65001 >nul
title NEPU_FAMS 固定资产管理系统

echo ======================================
echo   NEPU_FAMS 固定资产管理系统 - 一键启动
echo ======================================
echo.

:: 检查端口 8088
echo [1/4] 检查端口 8088...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8088 "') do (
    echo 端口 8088 被占用 (PID: %%a)，正在停止...
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
    echo 端口 8088 已释放
)

:: 检查端口 5173
echo [2/4] 检查端口 5173...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173 "') do (
    echo 端口 5173 被占用 (PID: %%a)，正在停止...
    taskkill /F /PID %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
    echo 端口 5173 已释放
)

:: 启动后端
echo [3/4] 启动后端 Spring Boot (Port 8088)...
start "NEPU_FAMS-Backend" "cmd /c mvnw.cmd spring-boot:run -q"

:: 等一会再启动前端
timeout /t 8 /nobreak >nul

:: 启动前端
echo [4/4] 启动前端 Vite (Port 5173)...
start "NEPU_FAMS-Frontend" "cmd /c cd /d frontend && npm run dev"

echo.
echo ======================================
echo   启动完成！
echo ======================================
echo.
echo   前端地址: http://localhost:5173
echo   后端地址: http://localhost:8088
echo.
echo   测试账号:
echo     admin / admin123     (超级管理员)
echo     college_admin1 / 123456  (学院管理员)
echo     user1 / 123456      (普通用户)
echo.
echo   按任意键打开浏览器并访问前端...
pause >nul

start http://localhost:5173
