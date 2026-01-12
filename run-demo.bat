@echo off
REM ═══════════════════════════════════════════════════════════════════════════════
REM  PQC CYBERSEC SIMULATOR - FULLY AUTOMATED DEMO
REM ═══════════════════════════════════════════════════════════════════════════════
REM  Starts all services, opens browsers, runs automated Selenium test
REM  NO USER INPUT REQUIRED - Everything runs automatically
REM
REM  HYBRID ENCRYPTION MODEL (Industry Standard - Like TLS/Signal/WhatsApp):
REM    - KEM: RSA-2048 or ML-KEM-768 (key encapsulation for AES key)
REM    - Bulk: AES-256-GCM (fast symmetric encryption for data)
REM    - Signature: RSA-2048 or ML-DSA-65 (authentication)
REM
REM  AUTHENTICATION:
REM    - Form-based login (demo accounts)
REM    - OAuth 2.0 ready (Google, GitHub) when configured
REM ═══════════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion
cd /d %~dp0
set "DEMO_DIR=%~dp0"

echo.
echo =============================================================================================
echo        PQC CYBERSEC - FULLY AUTOMATED DEMO
echo        Quantum-Resistant vs Classical Cryptography
echo =============================================================================================
echo.
echo   This demo runs AUTOMATICALLY - no input needed!
echo.
echo   HYBRID ENCRYPTION (Industry Standard):
echo     - KEM: RSA-2048 or ML-KEM-768 (key encapsulation)
echo     - Bulk: AES-256-GCM (fast symmetric encryption)
echo     - Signature: RSA-2048 or ML-DSA-65 (authentication)
echo.
echo   WORKFLOW:
echo     1. Start Quantum Simulator (GPU)
echo     2. Start Gov-Portal
echo     3. Start Hacker Console
echo     4. Open browser panels
echo     5. Run Selenium automated test
echo     6. Auto-cleanup after completion
echo.
echo =============================================================================================
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 1: Kill any existing processes and clean GPU
REM ═══════════════════════════════════════════════════════════════════════════════
echo [1/6] Cleaning up existing processes...
taskkill /F /IM java.exe >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1

if exist "%DEMO_DIR%Clear_GPU.py" (
    python "%DEMO_DIR%Clear_GPU.py" >nul 2>&1
    echo       [OK] GPU memory cleared
)
timeout /t 2 /nobreak >nul
echo       [OK] Cleanup complete

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 2: Start Quantum Simulator
REM ═══════════════════════════════════════════════════════════════════════════════
echo [2/6] Starting Quantum Simulator (GPU-accelerated)...
start "Quantum Simulator" /min cmd /c "cd /d %DEMO_DIR%quantum-simulator && python quantum_service.py"

set QS_RETRY=0
:WAIT_QS
timeout /t 3 /nobreak >nul
set /a QS_RETRY+=1
curl -s http://localhost:8184/api/quantum/status >nul 2>&1
if !errorlevel! equ 0 goto QS_OK
if !QS_RETRY! lss 15 (
    echo       Waiting for Quantum Simulator... [!QS_RETRY!/15]
    goto WAIT_QS
)
echo       [WARN] Quantum Simulator slow - continuing...
goto QS_DONE
:QS_OK
echo       [OK] Quantum Simulator running on http://localhost:8184
:QS_DONE

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 3: Start Gov-Portal
REM ═══════════════════════════════════════════════════════════════════════════════
echo [3/6] Starting Gov-Portal...
start "Gov-Portal" /min cmd /c "cd /d %DEMO_DIR%gov-portal && mvn spring-boot:run -Dspring-boot.run.profiles=h2 -q"

set GOV_RETRY=0
:WAIT_GOV
timeout /t 5 /nobreak >nul
set /a GOV_RETRY+=1
curl -s http://localhost:8181/ >nul 2>&1
if !errorlevel! equ 0 goto GOV_OK
if !GOV_RETRY! lss 24 (
    echo       Waiting for Gov-Portal... [!GOV_RETRY!/24]
    goto WAIT_GOV
)
echo       [ERROR] Gov-Portal failed to start!
goto CLEANUP
:GOV_OK
echo       [OK] Gov-Portal running on http://localhost:8181

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 4: Start Hacker Console
REM ═══════════════════════════════════════════════════════════════════════════════
echo [4/6] Starting Hacker Console...
if exist "%DEMO_DIR%hacker-console\hacker-data" rmdir /s /q "%DEMO_DIR%hacker-console\hacker-data" >nul 2>&1
start "Hacker Console" /min cmd /c "cd /d %DEMO_DIR%hacker-console && mvn spring-boot:run -Dspring-boot.run.profiles=standalone -q"

set HACKER_RETRY=0
:WAIT_HACKER
timeout /t 5 /nobreak >nul
set /a HACKER_RETRY+=1
curl -s http://localhost:8183/harvest >nul 2>&1
if !errorlevel! equ 0 goto HACKER_OK
if !HACKER_RETRY! lss 24 (
    echo       Waiting for Hacker Console... [!HACKER_RETRY!/24]
    goto WAIT_HACKER
)
echo       [ERROR] Hacker Console failed to start!
goto CLEANUP
:HACKER_OK
echo       [OK] Hacker Console running on http://localhost:8183

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 5: Open Browser Panels
REM ═══════════════════════════════════════════════════════════════════════════════
echo [5/6] Opening browser panels...
timeout /t 2 /nobreak >nul

start "" "http://localhost:8181"
timeout /t 1 /nobreak >nul
start "" "http://localhost:8181"
timeout /t 1 /nobreak >nul
start "" "http://localhost:8183/dashboard"
timeout /t 1 /nobreak >nul
start "" "http://localhost:8183/decrypt"
echo       [OK] All browser panels opened

echo.
echo =============================================================================================
echo   ALL SERVICES RUNNING!
echo =============================================================================================
echo.
echo   Gov-Portal:        http://localhost:8181
echo   Hacker Dashboard:  http://localhost:8183/dashboard
echo   Quantum Decrypt:   http://localhost:8183/decrypt
echo   Quantum API:       http://localhost:8184
echo.
echo   HYBRID ENCRYPTION MODEL:
echo     - KEM: RSA-2048 (vulnerable) or ML-KEM-768 (quantum-safe)
echo     - Bulk: AES-256-GCM (symmetric, fast for data)
echo     - Sig: RSA-2048 (vulnerable) or ML-DSA-65 (quantum-safe)
echo.
echo   AUTHENTICATION:
echo     - Form Login: john.citizen / Citizen@2024!
echo     - Form Login: officer / Officer@2024!
echo     - OAuth 2.0: Configure in application.properties
echo.
echo =============================================================================================
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 6: Run Automated Selenium Demo
REM ═══════════════════════════════════════════════════════════════════════════════
echo [6/6] Running automated Selenium demo...
echo.
echo   This demonstrates all 4 crypto scenarios automatically:
echo     1. RSA-KEM + AES-256 + RSA-Sig (FULLY VULNERABLE)
echo     2. ML-KEM + AES-256 + ML-DSA (FULLY QUANTUM-SAFE)
echo     3. RSA-KEM + AES-256 + ML-DSA (ENCRYPTION VULNERABLE)
echo     4. ML-KEM + AES-256 + RSA-Sig (SIGNATURE VULNERABLE)
echo.
echo   Watch the browser panels!
echo.

timeout /t 3 /nobreak >nul

cd /d "%DEMO_DIR%ui-tests"
call mvn test -Dtest=com.pqc.selenium.ComprehensiveCryptoTest
set TEST_RESULT=!errorlevel!
cd /d "%DEMO_DIR%"

echo.
if !TEST_RESULT! equ 0 (
    echo =============================================================================================
    echo   DEMO COMPLETED SUCCESSFULLY!
    echo =============================================================================================
) else (
    echo =============================================================================================
    echo   DEMO COMPLETED WITH SOME ISSUES (check test output above)
    echo =============================================================================================
)
echo.

REM Keep services running for 2 minutes for manual inspection
echo   Services will remain running for 2 minutes for manual inspection...
echo   Press any key to cleanup immediately, or wait for auto-cleanup.
echo.
timeout /t 120

:CLEANUP
echo.
echo =============================================================================================
echo   CLEANING UP...
echo =============================================================================================

echo   [1/4] Stopping service windows...
taskkill /FI "WINDOWTITLE eq Gov-Portal*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Hacker Console*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Quantum Simulator*" /F >nul 2>&1

echo   [2/4] Releasing network ports...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1

echo   [3/4] Stopping Java processes...
taskkill /F /IM java.exe >nul 2>&1
for /f "tokens=2" %%a in ('wmic process where "commandline like '%%quantum_service%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do taskkill /F /PID %%a >nul 2>&1

echo   [4/4] Releasing GPU VRAM...
if exist "%DEMO_DIR%Clear_GPU.py" (
    python "%DEMO_DIR%Clear_GPU.py" >nul 2>&1
    echo       [OK] GPU VRAM released
)

timeout /t 2 /nobreak >nul

echo.
echo =============================================================================================
echo   ALL CLEANED UP!
echo =============================================================================================
echo.
exit /b 0
