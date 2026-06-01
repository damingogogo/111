@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 重新打包 - 物资到货入库管理系统

py -V >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Python。请先安装 Python 3.10+ 并勾选 Add to PATH。
    pause
    exit /b 1
)

echo ============================================================
echo  正在检查打包依赖...
echo ============================================================
py -c "import PyInstaller" 2>nul
if errorlevel 1 (
    echo 安装 PyInstaller ...
    py -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple pyinstaller
)

py -c "import pymysql, openpyxl, ttkbootstrap, PIL" 2>nul
if errorlevel 1 (
    echo 安装运行依赖 ...
    py -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple pymysql openpyxl ttkbootstrap pillow
)

echo.
echo ============================================================
echo  开始打包...
echo ============================================================
py -m PyInstaller --clean --noconfirm build.spec
if errorlevel 1 (
    echo.
    echo [失败] 打包出错,请查看上方日志。
    pause
    exit /b 1
)

REM 复制一份中文名,方便用户辨识
if exist "dist\WarehouseMgmt.exe" (
    copy /Y "dist\WarehouseMgmt.exe" "dist\物资到货入库管理系统.exe" >nul
)

echo.
echo ============================================================
echo  打包完成! 产物在 dist\ 目录下
echo ============================================================
dir /B dist\*.exe
echo.
pause
