@echo off
REM ═══════════════════════════════════════════════════════════════════════════════
REM  PQC CYBERSEC SIMULATOR - COMPLETE AUTOMATED DEMO
REM ═══════════════════════════════════════════════════════════════════════════════
REM  ONE-CLICK DEMO: Just run this file and watch the 4 browser panels!
REM  AUTO-CLEANUP: Services auto-shutdown after 5 minutes of inactivity
REM  GPU CLEANUP: GPU VRAM is automatically released on exit
REM ═══════════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion
cd /d %~dp0

REM Set auto-shutdown timeout (in seconds) - 5 minutes = 300 seconds
set AUTO_SHUTDOWN_TIMEOUT=300

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
echo   AUTO-CLEANUP: Services will auto-shutdown after 5 minutes of inactivity
echo   GPU CLEANUP:  GPU VRAM will be released when services stop
echo =============================================================================================
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 1: Kill any existing processes and clean GPU
REM ═══════════════════════════════════════════════════════════════════════════════
echo [1/5] Cleaning up existing processes and GPU memory...
taskkill /F /IM java.exe >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1

REM Clean GPU memory if Python and script available
if exist "%~dp0Clear_GPU.py" (
    python "%~dp0Clear_GPU.py" >nul 2>&1
    echo    [OK] GPU memory cleared
)
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
echo   AUTO-SHUTDOWN: Services will stop in 5 minutes if no action is taken.
echo   Press any key within 5 minutes to keep services running, or wait for auto-cleanup.
echo.
echo   Options:
echo     [O] - Open browsers and reset timer
echo     [Q] - Quit immediately and cleanup
echo     [Any other key] - Keep services running (no auto-shutdown)
echo.

REM Start auto-shutdown timer in background
set "TIMER_ACTIVE=1"
set "COUNTDOWN=%AUTO_SHUTDOWN_TIMEOUT%"

:MENU_LOOP
echo.
echo   Waiting for input (auto-shutdown in !COUNTDOWN! seconds)...
choice /c OQK /t 30 /d K /n /m "   Press [O]pen browsers, [Q]uit, or [K]eep running: "
set USER_CHOICE=!errorlevel!

if !USER_CHOICE! equ 1 (
    echo    Opening browsers...
    start "" "http://localhost:8181"
    start "" "http://localhost:8183"
    start "" "http://localhost:8183/decrypt"
    echo    Browsers opened! Timer reset.
    set "COUNTDOWN=%AUTO_SHUTDOWN_TIMEOUT%"
    goto MENU_LOOP
)

if !USER_CHOICE! equ 2 (
    echo    User requested quit...
    goto CLEANUP
)

if !USER_CHOICE! equ 3 (
    REM Timeout occurred (30 seconds passed)
    set /a COUNTDOWN=!COUNTDOWN!-30
    if !COUNTDOWN! leq 0 (
        echo.
        echo    [AUTO-SHUTDOWN] 5 minutes of inactivity - cleaning up resources...
        goto CLEANUP
    )
    goto MENU_LOOP
)

REM If user pressed any other key, disable auto-shutdown
echo    Auto-shutdown disabled. Press Ctrl+C or close window to stop services.
echo.
echo   Browsers opened! Press any key when done to stop all services...
start "" "http://localhost:8181"
start "" "http://localhost:8183"
start "" "http://localhost:8183/decrypt"
pause >nul

:CLEANUP
echo.
echo =============================================================================================
echo   SHUTTING DOWN AND CLEANING UP RESOURCES...
echo =============================================================================================

REM Kill all service windows
echo    [1/4] Stopping service windows...
taskkill /FI "WINDOWTITLE eq Gov-Portal*" >nul 2>&1
taskkill /FI "WINDOWTITLE eq Hacker Console*" >nul 2>&1
taskkill /FI "WINDOWTITLE eq Quantum Simulator*" >nul 2>&1

REM Kill processes by port
echo    [2/4] Releasing network ports...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1

REM Kill any remaining Python processes for quantum simulator
echo    [3/4] Stopping Python processes...
taskkill /F /IM python.exe /FI "WINDOWTITLE eq *quantum*" >nul 2>&1
for /f "tokens=2" %%a in ('wmic process where "commandline like '%%quantum_service%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do taskkill /F /PID %%a >nul 2>&1

REM Clean GPU VRAM
echo    [4/4] Releasing GPU VRAM...
if exist "%~dp0Clear_GPU.py" (
    python "%~dp0Clear_GPU.py" >nul 2>&1
    if !errorlevel! equ 0 (
        echo    [OK] GPU VRAM released successfully
    ) else (
        echo    [INFO] GPU cleanup script completed
    )
) else (
    echo    [INFO] GPU cleanup script not found - skipping
)

REM Wait a moment for cleanup to complete
timeout /t 2 /nobreak >nul

echo.
echo =============================================================================================
echo   ALL RESOURCES CLEANED UP!
echo =============================================================================================
echo   - All services stopped
echo   - Network ports released (8181, 8183, 8184)
echo   - GPU VRAM released
echo =============================================================================================
echo.
pause
exit /b 0
