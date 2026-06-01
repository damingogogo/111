@echo off
cd /d "%~dp0"
npm.cmd install
npm.cmd run build:mp-weixin
echo.
echo If HBuilderX still reports missing compiler modules, close and reopen HBuilderX, then import this folder:
echo %cd%
