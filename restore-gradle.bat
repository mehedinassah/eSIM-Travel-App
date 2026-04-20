@echo off
REM Gradle Wrapper Restoration Script
REM This script downloads and restores the gradle wrapper

setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo ========================================
echo Gradle Wrapper Restoration Script
echo ========================================
echo.

REM Check if gradle wrapper jar exists
if exist "gradle\wrapper\gradle-8.13\bin\gradle.bat" (
    echo [OK] Gradle 8.13 already installed
    goto BUILD
)

echo [INFO] Gradle wrapper jar not found. Attempting to restore...
echo.

REM Try to download using powershell
echo [INFO] Downloading gradle-8.13...

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference = 'SilentlyContinue'; ^
    try { ^
        Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.13-bin.zip' ^
            -OutFile 'gradle-8.13-bin.zip' -TimeoutSec 60; ^
        Write-Host '[OK] Download complete'; ^
    } catch { ^
        Write-Host '[ERROR] Download failed'; ^
        exit 1; ^
    }" 2>nul

if not exist "gradle-8.13-bin.zip" (
    echo [ERROR] Failed to download gradle. Please check your internet connection.
    echo [INFO] Alternatively, you can:
    echo        1. Open the project in Android Studio
    echo        2. Android Studio will automatically sync and download gradle
    goto END
)

echo [INFO] Extracting gradle...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Path 'gradle-8.13-bin.zip' -DestinationPath 'gradle\wrapper' -Force" 2>nul

if exist "gradle\wrapper\gradle-8.13\bin\gradle.bat" (
    echo [OK] Gradle 8.13 extracted successfully
    del /q gradle-8.13-bin.zip
    goto BUILD
) else (
    echo [ERROR] Failed to extract gradle
    goto END
)

:BUILD
echo.
echo [INFO] Build configuration restored. You can now:
echo        1. Run: gradlew.bat clean build
echo        2. Or open the project in Android Studio and build from there
echo.

:END
echo Done.
pause
