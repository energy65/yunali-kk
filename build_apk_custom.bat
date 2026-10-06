@echo off
cd /d "%~dp0"
call gradlew.bat assembleLeanbackArmeabi_v7aRelease assembleMobileArm64_v8aRelease --no-daemon