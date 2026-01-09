@echo off
REM ═══════════════════════════════════════════════════════════════════════════════
REM  PQC CYBERSEC SIMULATOR - COMPLETE AUTOMATED DEMO
REM ═══════════════════════════════════════════════════════════════════════════════
REM  ONE-CLICK DEMO: Just run this file and watch the 4 browser panels!
REM ═══════════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion
cd /d %~dp0

echo.
echo =============================================================================================
echo        PQC CYBERSEC - COMPREHENSIVE 4-PANEL DEMO (ONE-CLICK)
echo        Demonstrating Quantum-Resistant vs Classical Cryptography
echo =============================================================================================
echo   ALL 4 SCENARIOS WILL BE DEMONSTRATED:
echo     1. RSA KEM + RSA Sig     - FULLY VULNERABLE (Both broken by quantum)
echo     2. ML-KEM + ML-DSA       - FULLY QUANTUM-SAFE (Both protected)
echo     3. RSA KEM + ML-DSA      - MIXED (Encryption vulnerable, Signature safe)
echo     4. ML-KEM + RSA Sig      - MIXED (Encryption safe, Signature vulnerable)
echo =============================================================================================
echo   4 BROWSER PANELS:
echo     TOP-LEFT:     Citizen Portal (submit documents)
echo     TOP-RIGHT:    Officer Portal (review applications)
echo     BOTTOM-LEFT:  Hacker Harvest (intercept traffic)
echo     BOTTOM-RIGHT: Hacker Decrypt (quantum attack progress)
echo =============================================================================================
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 1: Kill any existing processes
REM ═══════════════════════════════════════════════════════════════════════════════
echo [1/5] Cleaning up existing processes...
taskkill /F /IM java.exe >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
timeout /t 3 /nobreak >nul
echo    [OK] Cleanup complete

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 2: Start Quantum Simulator
REM ═══════════════════════════════════════════════════════════════════════════════
echo [2/5] Starting Quantum Simulator (GPU-accelerated)...
start "Quantum Simulator (GPU)" cmd /c "cd /d %~dp0quantum-simulator && python quantum_service.py"

set QS_RETRY=0
:WAIT_QS
timeout /t 3 /nobreak >nul
set /a QS_RETRY+=1
curl -s http://localhost:8184/api/quantum/status >nul 2>&1
if !errorlevel! equ 0 goto QS_OK
if !QS_RETRY! lss 10 (
    echo    Waiting for Quantum Simulator... [!QS_RETRY!/10]
    goto WAIT_QS
)
echo    [WARN] Quantum Simulator slow to start - continuing...
goto QS_DONE
:QS_OK
echo    [OK] Quantum Simulator running on http://localhost:8184
:QS_DONE

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 3: Start Gov-Portal (with H2 in-memory database)
REM ═══════════════════════════════════════════════════════════════════════════════
echo [3/5] Starting Gov-Portal (H2 in-memory database)...
start "Gov-Portal (8181)" cmd /c "cd /d %~dp0gov-portal && mvn spring-boot:run -Dspring-boot.run.profiles=h2"

set GOV_RETRY=0
:WAIT_GOV
timeout /t 5 /nobreak >nul
set /a GOV_RETRY+=1
curl -s http://localhost:8181/ >nul 2>&1
if !errorlevel! equ 0 goto GOV_OK
if !GOV_RETRY! lss 20 (
    echo    Waiting for Gov-Portal... [!GOV_RETRY!/20]
    goto WAIT_GOV
)
echo    [ERROR] Gov-Portal failed to start!
pause
exit /b 1
:GOV_OK
echo    [OK] Gov-Portal running on http://localhost:8181

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 4: Start Hacker Console
REM ═══════════════════════════════════════════════════════════════════════════════
echo [4/5] Starting Hacker Console (standalone mode)...
if exist "%~dp0hacker-console\hacker-data" rmdir /s /q "%~dp0hacker-console\hacker-data" >nul 2>&1
start "Hacker Console (8183)" cmd /c "cd /d %~dp0hacker-console && mvn spring-boot:run -Dspring-boot.run.profiles=standalone"

set HACKER_RETRY=0
:WAIT_HACKER
timeout /t 5 /nobreak >nul
set /a HACKER_RETRY+=1
curl -s http://localhost:8183/harvest >nul 2>&1
if !errorlevel! equ 0 goto HACKER_OK
if !HACKER_RETRY! lss 20 (
    echo    Waiting for Hacker Console... [!HACKER_RETRY!/20]
    goto WAIT_HACKER
)
echo    [ERROR] Hacker Console failed to start!
pause
exit /b 1
:HACKER_OK
echo    [OK] Hacker Console running on http://localhost:8183

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 5: Run Selenium Tests (4 Browser Panels)
REM ═══════════════════════════════════════════════════════════════════════════════
echo [5/5] Running 4-Panel Selenium Demo...
echo.
echo =============================================================================================
echo   ALL SERVICES RUNNING - STARTING 4-PANEL AUTOMATED DEMO!
echo =============================================================================================
echo   Watch the 4 Chrome windows that will open:
echo     - Top-Left:     Citizen submits applications
echo     - Top-Right:    Officer reviews them
echo     - Bottom-Left:  Hacker intercepts encrypted traffic
echo     - Bottom-Right: Quantum attack decryption progress
echo =============================================================================================
echo.

cd /d %~dp0ui-tests
call mvn test -Dtest=com.pqc.selenium.ComprehensiveCryptoTest
cd /d %~dp0

echo.
echo =============================================================================================
echo   DEMO COMPLETE! All 4 browser panels remain open for inspection.
echo =============================================================================================
echo   SERVICES ARE STILL RUNNING:
echo     - Gov-Portal:        http://localhost:8181
echo     - Hacker Console:    http://localhost:8183
echo     - Quantum Simulator: http://localhost:8184
echo =============================================================================================
echo.
echo   Press any key to open browsers manually, or Q to quit...
set /p "USER_CHOICE="
if /i "!USER_CHOICE!"=="Q" goto CLEANUP

start "" "http://localhost:8181"
start "" "http://localhost:8183"
start "" "http://localhost:8183/decrypt"

echo.
echo   Browsers opened! Press any key when done to stop all services...
pause >nul

:CLEANUP
echo.
echo =============================================================================================
echo   SHUTTING DOWN...
echo =============================================================================================
taskkill /FI "WINDOWTITLE eq Gov-Portal*" >nul 2>&1
taskkill /FI "WINDOWTITLE eq Hacker Console*" >nul 2>&1
taskkill /FI "WINDOWTITLE eq Quantum Simulator*" >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
echo    All services stopped.
pause
exit /b 0
