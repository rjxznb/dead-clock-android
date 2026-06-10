@echo off
title OneLife Emulator
set ANDROID_HOME=C:\Android\sdk
echo Starting OneLife emulator, keep this window open...
C:\Android\sdk\emulator\emulator.exe -avd OneLife -gpu host
pause
