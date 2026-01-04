@echo off
REM ═══════════════════════════════════════════════════════════════════════════════
REM  PQC CYBERSEC SIMULATOR - 4-PANEL DEMO (Windows)
REM ═══════════════════════════════════════════════════════════════════════════════
REM  Demonstrates Post-Quantum Cryptography vs Classical Encryption
REM  
REM  4 Browser Panels:
REM    TOP-LEFT:     Citizen - Submits government documents
REM    TOP-RIGHT:    Officer - Reviews and approves applications  
REM    BOTTOM-LEFT:  Hacker Harvest - Intercepts encrypted traffic
REM    BOTTOM-RIGHT: Hacker Decrypt - Quantum attack progress
REM ═══════════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion

echo.
echo ╔══════════════════════════════════════════════════════════════════╗
echo ║        PQC CYBERSEC - 4-PANEL DEMO LAUNCHER                      ║
echo ║        Demonstrating Quantum-Resistant Cryptography              ║
echo ╚══════════════════════════════════════════════════════════════════╝
echo.

REM Step 1: Check Docker
echo [1/5] Checking Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running! Please start Docker Desktop.
    pause
    exit /b 1
)
echo [OK] Docker is running

REM Step 2: Restart Docker services with fresh database
echo [2/5] Starting Docker services with fresh database...
docker-compose down -v >nul 2>&1
docker-compose up -d postgres >nul 2>&1
timeout /t 8 /nobreak >nul
docker-compose up -d gov-portal secure-messaging >nul 2>&1
echo [INFO] Waiting 25 seconds for services...
timeout /t 25 /nobreak >nul
echo [OK] Docker services started

REM Step 3: Stop existing hacker-console
echo [3/5] Preparing hacker-console...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
if exist "%~dp0hacker-console\hacker-data" rmdir /s /q "%~dp0hacker-console\hacker-data" >nul 2>&1
timeout /t 2 /nobreak >nul
echo [OK] Ready

REM Step 4: Start hacker-console
echo [4/5] Starting Hacker Console...
start "Hacker Console" cmd /k "cd /d %~dp0hacker-console && mvn spring-boot:run -Dspring-boot.run.profiles=standalone"

set RETRY=0
:WAIT_LOOP
timeout /t 5 /nobreak >nul
set /a RETRY+=1
curl -s http://localhost:8183/harvest >nul 2>&1
if %errorlevel% equ 0 goto HACKER_OK
if %RETRY% lss 12 (
    echo [INFO] Waiting for Hacker Console... [%RETRY%/12]
    goto WAIT_LOOP
)
echo [ERROR] Hacker Console failed to start!
pause
exit /b 1

:HACKER_OK
echo [OK] Hacker Console running

REM Step 5: Run test
echo [5/5] Running 4-Panel Demo...
echo.
echo ════════════════════════════════════════════════════════════════════
echo   WATCH THE 4 BROWSER PANELS:
echo   - Top-Left:     Citizen submits documents
echo   - Top-Right:    Officer reviews applications
echo   - Bottom-Left:  Hacker intercepts encrypted traffic
echo   - Bottom-Right: Quantum decryption attack
echo ════════════════════════════════════════════════════════════════════
echo.

cd /d %~dp0ui-tests
echo.
echo Running Selenium test - Watch the 4 browser panels!
echo.
call mvn test -Dtest=com.pqc.selenium.FourPanelRealisticDemoTest

echo.
echo ════════════════════════════════════════════════════════════════════
echo   Demo Complete! Hacker Console window remains open.
echo ════════════════════════════════════════════════════════════════════
pause
