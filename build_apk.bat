@echo off
cd /d "%~dp0"
gradlew.bat assembleLeanbackArmeabi_v7aRelease assembleMobileArm64_v8aRelease --no-daemon
