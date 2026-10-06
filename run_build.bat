@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Current directory: %cd%
echo Running gradle...
call gradlew.bat assembleLeanbackArmeabi_v7aRelease assembleMobileArm64_v8aRelease --no-daemon
echo Exit code: %errorlevel%
pause
