@echo off
REM ═══════════════════════════════════════════════════════════════════════════════
REM  PQC CYBERSEC SIMULATOR - COMPREHENSIVE 4-PANEL DEMO
REM ═══════════════════════════════════════════════════════════════════════════════
REM  Usage:
REM    run-demo.bat           - Auto-detect (Docker if available, else local)
REM    run-demo.bat --local   - Force local on-premise mode (no Docker)
REM    run-demo.bat --docker  - Force Docker mode
REM
REM  Runs ALL 4 scenarios comparing Classical vs PQC cryptography
REM ═══════════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion

REM Parse command line arguments
set "FORCE_LOCAL=0"
set "FORCE_DOCKER=0"
if /i "%~1"=="--local" set "FORCE_LOCAL=1"
if /i "%~1"=="-l" set "FORCE_LOCAL=1"
if /i "%~1"=="--onprem" set "FORCE_LOCAL=1"
if /i "%~1"=="--docker" set "FORCE_DOCKER=1"
if /i "%~1"=="-d" set "FORCE_DOCKER=1"

echo.
echo =============================================================================================
echo        PQC CYBERSEC - COMPREHENSIVE 4-PANEL DEMO
echo        Demonstrating Quantum-Resistant vs Classical Cryptography
echo =============================================================================================
echo   ALL 4 SCENARIOS WILL RUN:
echo     1. RSA KEM + RSA Sig     - FULLY VULNERABLE (Both broken by quantum)
echo     2. ML-KEM + ML-DSA       - FULLY QUANTUM-SAFE (Both protected)
echo     3. RSA KEM + ML-DSA      - MIXED (Encryption vulnerable, Signature safe)
echo     4. ML-KEM + RSA Sig      - MIXED (Encryption safe, Signature vulnerable)
echo =============================================================================================
echo   Usage: run-demo.bat [--local ^| --docker]
echo     --local, -l, --onprem : Force local on-premise mode (SQLite database, no Docker)
echo     --docker, -d          : Force Docker mode (PostgreSQL in container)
echo     (no flag)             : Auto-detect infrastructure
echo =============================================================================================
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 1: Check Prerequisites and Determine Infrastructure
REM ═══════════════════════════════════════════════════════════════════════════════
echo [1/6] Checking Prerequisites and Determining Infrastructure...

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo    [ERROR] Java is not installed! Please install JDK 17+.
    pause
    exit /b 1
)
echo    [OK] Java found

REM Check Maven
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo    [ERROR] Maven is not installed! Please install Apache Maven.
    pause
    exit /b 1
)
echo    [OK] Maven found

REM Check Python for quantum simulator
set "PYTHON_AVAILABLE=0"
python --version >nul 2>&1
if !errorlevel! equ 0 (
    echo    [OK] Python found
    set "PYTHON_AVAILABLE=1"
) else (
    echo    [WARN] Python not found - Quantum simulator will use simulation mode
)

REM Determine deployment mode
set "USE_DOCKER=0"

if "!FORCE_LOCAL!"=="1" (
    echo    [INFO] Forced LOCAL mode via command line
    set "USE_DOCKER=0"
    goto MODE_DETERMINED
)

if "!FORCE_DOCKER!"=="1" (
    docker info >nul 2>&1
    if !errorlevel! equ 0 (
        echo    [INFO] Forced DOCKER mode via command line
        set "USE_DOCKER=1"
    ) else (
        echo    [ERROR] Docker requested but not available!
        pause
        exit /b 1
    )
    goto MODE_DETERMINED
)

REM Auto-detect Docker availability
docker info >nul 2>&1
if !errorlevel! equ 0 (
    echo    [OK] Docker detected and running (auto-detected)
    set "USE_DOCKER=1"
) else (
    echo    [INFO] Docker not available - using local on-premise mode
)

:MODE_DETERMINED
echo.
if "!USE_DOCKER!"=="1" (
    echo    === DEPLOYMENT MODE: DOCKER CONTAINERS ===
    echo    Database: PostgreSQL (containerized)
    echo    Gov-Portal: Docker container
) else (
    echo    === DEPLOYMENT MODE: LOCAL ON-PREMISE ===
    echo    Database: SQLite (file-based, persistent)
    echo    Gov-Portal: Local Maven process
)
echo    Hacker Console: Local Maven process (always on-premise)
echo.

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 2: Start Database
REM ═══════════════════════════════════════════════════════════════════════════════
echo [2/6] Starting Database...

if "!USE_DOCKER!"=="1" goto DB_DOCKER
goto DB_LOCAL

:DB_DOCKER
echo    Starting PostgreSQL via Docker...
docker-compose down -v >nul 2>&1
docker-compose up -d postgres >nul 2>&1
echo    [INFO] Waiting for PostgreSQL container...
timeout /t 10 /nobreak >nul
echo    [OK] PostgreSQL started (Docker)
set "DB_PROFILE=docker"
goto DB_DONE

:DB_LOCAL
echo    [OK] Using SQLite database (file-based, persistent)
REM Create data directory for SQLite
if not exist "%~dp0gov-portal\data" mkdir "%~dp0gov-portal\data"
set "DB_PROFILE=sqlite"
goto DB_DONE

:DB_DONE

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 3: Deploy Gov-Portal Service
REM ═══════════════════════════════════════════════════════════════════════════════
echo [3/6] Deploying Gov-Portal Service...

REM Kill existing processes on port 8181
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8181.*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

if "!USE_DOCKER!"=="1" goto GOV_DOCKER
goto GOV_LOCAL

:GOV_DOCKER
echo    Starting Gov-Portal container...
docker-compose up -d gov-portal >nul 2>&1
echo    [INFO] Waiting for Gov-Portal container to be healthy...
set GOV_RETRY=0
:WAIT_GOV_DOCKER
timeout /t 5 /nobreak >nul
set /a GOV_RETRY+=1
curl -s http://localhost:8181/ >nul 2>&1
if !errorlevel! equ 0 goto GOV_OK
if !GOV_RETRY! lss 12 (
    echo    [INFO] Waiting for Gov-Portal... [!GOV_RETRY!/12]
    goto WAIT_GOV_DOCKER
)
echo    [ERROR] Gov-Portal container failed to start!
docker-compose logs gov-portal
pause
exit /b 1

:GOV_LOCAL
echo    Starting Gov-Portal locally with !DB_PROFILE! profile...
start "Gov-Portal" cmd /c "cd /d %~dp0gov-portal && mvn spring-boot:run -Dspring-boot.run.profiles=!DB_PROFILE!"

set GOV_RETRY=0
:WAIT_GOV_LOCAL
timeout /t 5 /nobreak >nul
set /a GOV_RETRY+=1
curl -s http://localhost:8181/ >nul 2>&1
if !errorlevel! equ 0 goto GOV_OK
if !GOV_RETRY! lss 15 (
    echo    [INFO] Waiting for Gov-Portal... [!GOV_RETRY!/15]
    goto WAIT_GOV_LOCAL
)
echo    [ERROR] Gov-Portal failed to start!
pause
exit /b 1

:GOV_OK
echo    [OK] Gov-Portal running on http://localhost:8181

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 4: Deploy Hacker Console UI (ALWAYS LOCAL - simulates external attacker)
REM ═══════════════════════════════════════════════════════════════════════════════
echo [4/6] Deploying Hacker Console UI (Local On-Premise)...

REM Kill existing processes on port 8183
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8183.*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
if exist "%~dp0hacker-console\hacker-data" rmdir /s /q "%~dp0hacker-console\hacker-data" >nul 2>&1
timeout /t 2 /nobreak >nul

REM Hacker Console ALWAYS runs locally to simulate external threat actor
echo    Starting Hacker Console locally (standalone mode - external attacker)...
start "Hacker Console" cmd /k "cd /d %~dp0hacker-console && mvn spring-boot:run -Dspring-boot.run.profiles=standalone"

set HACKER_RETRY=0
:WAIT_HACKER_LOCAL
timeout /t 5 /nobreak >nul
set /a HACKER_RETRY+=1
curl -s http://localhost:8183/harvest >nul 2>&1
if !errorlevel! equ 0 goto HACKER_OK
if !HACKER_RETRY! lss 12 (
    echo    [INFO] Waiting for Hacker Console... [!HACKER_RETRY!/12]
    goto WAIT_HACKER_LOCAL
)
echo    [ERROR] Hacker Console failed to start!
pause
exit /b 1

:HACKER_OK
echo    [OK] Hacker Console UI running on http://localhost:8183 (Local)

REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 5: Deploy Quantum Simulator
REM ═══════════════════════════════════════════════════════════════════════════════
echo [5/6] Deploying Quantum Simulator...

REM Kill existing processes on port 8184
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8184.*LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

REM Quantum Simulator always runs locally (Python)
if "!PYTHON_AVAILABLE!"=="1" goto QUANTUM_LOCAL
goto SKIP_QUANTUM

:QUANTUM_LOCAL
echo    Starting Quantum Simulator locally (Python)...
start "Quantum Simulator" cmd /k "cd /d %~dp0quantum-simulator && python quantum_service.py"

set QS_RETRY=0
:WAIT_QS_LOCAL
timeout /t 3 /nobreak >nul
set /a QS_RETRY+=1
curl -s http://localhost:8184/api/quantum/status >nul 2>&1
if !errorlevel! equ 0 goto QUANTUM_OK
if !QS_RETRY! lss 8 (
    echo    [INFO] Waiting for Quantum Simulator... [!QS_RETRY!/8]
    goto WAIT_QS_LOCAL
)
echo    [WARN] Quantum Simulator not available - using simulation mode
goto SKIP_QUANTUM

:QUANTUM_OK
echo    [OK] Quantum Simulator running on http://localhost:8184
goto RUN_TESTS

:SKIP_QUANTUM
echo    [INFO] Proceeding with simulation mode for quantum attacks

:RUN_TESTS
REM ═══════════════════════════════════════════════════════════════════════════════
REM Step 6: Run Comprehensive 4-Panel Demo (ALL SCENARIOS)
REM ═══════════════════════════════════════════════════════════════════════════════
echo [6/6] Running Comprehensive 4-Panel Demo...
echo.
echo =============================================================================================
echo   DEPLOYMENT SUMMARY:
if "!USE_DOCKER!"=="1" (
    echo     Infrastructure: Docker Containers + Local Services
    echo     Database:       PostgreSQL (containerized)
    echo     Gov-Portal:     Docker container
) else (
    echo     Infrastructure: Fully Local On-Premise (No Docker)
    echo     Database:       SQLite (file-based, persistent)
    echo     Gov-Portal:     Local Maven process
)
echo     Hacker Console: Local Maven process (simulates external attacker)
echo     Quantum Sim:    Local Python process
echo.
echo   SERVICES DEPLOYED:
echo     Gov-Portal:        http://localhost:8181
echo     Hacker Console UI: http://localhost:8183
echo     Quantum Simulator: http://localhost:8184
echo.
echo   WATCH THE 4 BROWSER PANELS:
echo     Top-Left:     Citizen submits documents with different algorithms
echo     Top-Right:    Officer reviews applications
echo     Bottom-Left:  Hacker intercepts encrypted traffic
echo     Bottom-Right: Quantum decryption attack progress
echo.
echo   RUNNING ALL 4 SCENARIOS:
echo     Scenario 1: RSA + RSA       - FULLY VULNERABLE
echo     Scenario 2: ML-KEM + ML-DSA - FULLY QUANTUM-SAFE
echo     Scenario 3: RSA + ML-DSA    - MIXED
echo     Scenario 4: ML-KEM + RSA    - MIXED
echo =============================================================================================
echo.

cd /d %~dp0ui-tests
echo Starting Selenium tests - Watch the 4 browser panels!
echo.

REM Run comprehensive test with all scenarios
call mvn test -Dtest=com.pqc.selenium.ComprehensiveCryptoTest

echo.
echo =============================================================================================
echo   DEMO COMPLETE!
echo.
echo   Services remain running for manual exploration:
echo     Gov-Portal:        http://localhost:8181
echo     Hacker Console UI: http://localhost:8183
echo     Quantum Simulator: http://localhost:8184
echo.
echo   To stop all services:
if "!USE_DOCKER!"=="1" (
    echo     docker-compose down
    echo     Close the Hacker Console and Quantum Simulator terminal windows
) else (
    echo     Close the terminal windows or run: taskkill /F /IM java.exe
)
echo =============================================================================================
pause
