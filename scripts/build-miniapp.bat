@echo off
cd /d "%~dp0..\mobile-uniapp"
npm.cmd install
npm.cmd run build:mp-weixin
echo.
echo WeChat Mini Program output:
echo %cd%\dist\build\mp-weixin
