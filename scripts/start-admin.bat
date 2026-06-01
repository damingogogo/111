@echo off
cd /d "%~dp0..\admin-web"
npm.cmd install
npm.cmd run dev
