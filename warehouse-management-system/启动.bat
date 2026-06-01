@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 物资到货入库管理系统

echo ========================================
echo   物资到货入库管理系统
echo ========================================
echo.

python -V >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Python。
    echo 请先安装 Python 3.10+ 并勾选 "Add Python to PATH"。
    echo 下载地址: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo 正在检查依赖...
python -c "import pymysql, openpyxl, tkinter, ttkbootstrap, PIL" 2>nul
if errorlevel 1 (
    echo 正在安装依赖...
    python -m pip install pymysql openpyxl ttkbootstrap pillow
    if errorlevel 1 (
        echo [错误] 依赖安装失败。
        pause
        exit /b 1
    )
)

echo.
echo 正在启动程序...
python main.py
if errorlevel 1 (
    echo.
    echo 程序异常退出，按任意键关闭...
    pause >nul
)
