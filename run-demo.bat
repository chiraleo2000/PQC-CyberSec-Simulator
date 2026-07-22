@echo off
REM ═══════════════════════════════════════════════════════════════════════════════
REM  PQC CYBERSEC SIMULATOR - FAST DEMO LAUNCHER
REM  Usage:
REM    run-demo.bat              - EXE mode (default), run Selenium, leave services up
REM    run-demo.bat exe          - Native Maven/Python (same as default)
REM    run-demo.bat docker       - Docker compose mode
REM    run-demo.bat --skip-tests - Start services + browsers only (no Selenium)
REM    run-demo.bat --menu       - Keep interactive menu after start
REM  No interactive waits. Mode defaults to EXE immediately.
REM ═══════════════════════════════════════════════════════════════════════════════

setlocal EnableDelayedExpansion
cd /d "%~dp0"
set "DEMO_DIR=%~dp0"
set "DEMO_MODE=EXE"
set "SKIP_TESTS=0"
set "KEEP_MENU=0"

for %%A in (%*) do (
    if /I "%%~A"=="docker" set "DEMO_MODE=DOCKER"
    if /I "%%~A"=="--docker" set "DEMO_MODE=DOCKER"
    if /I "%%~A"=="exe" set "DEMO_MODE=EXE"
    if /I "%%~A"=="--exe" set "DEMO_MODE=EXE"
    if /I "%%~A"=="--skip-tests" set "SKIP_TESTS=1"
    if /I "%%~A"=="--menu" set "KEEP_MENU=1"
    if /I "%%~A"=="cleanup" goto CLEANUP
    if /I "%%~A"=="--cleanup" goto CLEANUP
)

echo.
echo =============================================================================================
echo   PQC CYBERSEC DEMO  ^|  MODE=!DEMO_MODE!  ^|  FAST START (no waits)
echo =============================================================================================
echo.

if /I "!DEMO_MODE!"=="DOCKER" goto DOCKER_MODE

REM ═══════════════════════════════════════════════════════════════════════════════
REM EXE MODE
REM ═══════════════════════════════════════════════════════════════════════════════
echo [0/5] Prerequisites...

REM Prefer JDK 25
for /d %%D in ("C:\Program Files\Java\jdk-25*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVA_HOME=%%D"
        set "PATH=%%D\bin;!PATH!"
    )
)
where javac >nul 2>&1
if errorlevel 1 (
    set "FOUND_JDK="
    for /d %%D in ("C:\Program Files\Java\jdk-*") do if exist "%%D\bin\javac.exe" set "FOUND_JDK=%%D"
    if defined FOUND_JDK (
        set "JAVA_HOME=!FOUND_JDK!"
        set "PATH=!FOUND_JDK!\bin;!PATH!"
    ) else (
        echo [ERROR] JDK 25+ required. Set JAVA_HOME.
        exit /b 1
    )
)
echo       [OK] JAVA_HOME=!JAVA_HOME!

REM CUDA 13.3 for CuPy JIT
set "CUDA_PATH="
if exist "C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3\include\cuda.h" (
    set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3"
) else if exist "C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.2\include\cuda.h" (
    set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.2"
)
if defined CUDA_PATH (
    set "CUDA_HOME=!CUDA_PATH!"
    set "PATH=!CUDA_PATH!\bin;!CUDA_PATH!\bin\x64;!PATH!"
    echo       [OK] CUDA_PATH=!CUDA_PATH!
)

REM Build fat jars if missing (one-time / after clean)
if not exist "%DEMO_DIR%gov-portal\target\gov-portal-1.0.1.jar" goto NEED_BUILD
if not exist "%DEMO_DIR%hacker-console\target\hacker-console-1.0.1.jar" goto NEED_BUILD
echo       [OK] Fat jars present
goto AFTER_BUILD

:NEED_BUILD
echo       [INFO] Building fat jars (skip tests)...
cd /d "%DEMO_DIR%"
call mvn -q -DskipTests package
if errorlevel 1 (
    echo [ERROR] Maven package failed.
    exit /b 1
)
echo       [OK] Build complete
:AFTER_BUILD

echo [1/5] Freeing ports 8181-8184...
if exist "%DEMO_DIR%docker-compose.yml" (
    pushd "%DEMO_DIR%"
    docker compose down >nul 2>&1
    popd
)
for %%P in (8181 8182 8183 8184) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
)
taskkill /F /FI "WINDOWTITLE eq Quantum Simulator*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Gov-Portal*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Hacker Console*" >nul 2>&1

echo [2/5] Starting Quantum + Gov + Hacker in PARALLEL...
if exist "%DEMO_DIR%hacker-console\hacker-data" rmdir /s /q "%DEMO_DIR%hacker-console\hacker-data" >nul 2>&1
start "Quantum Simulator" /min "%DEMO_DIR%quantum-simulator\start-quantum-fast.bat"
start "Gov-Portal" /min java -jar "%DEMO_DIR%gov-portal\target\gov-portal-1.0.1.jar" --spring.profiles.active=h2
start "Hacker Console" /min java -jar "%DEMO_DIR%hacker-console\target\hacker-console-1.0.1.jar" --spring.profiles.active=standalone

echo [3/5] Fast-polling readiness (1s)...
set /a READY_TRY=0
:WAIT_ALL
set /a READY_TRY+=1
set "QS_OK=0" & set "GOV_OK=0" & set "HACK_OK=0"
curl -s -m 1 http://127.0.0.1:8184/api/quantum/status >nul 2>&1 && set "QS_OK=1"
curl -s -m 1 http://127.0.0.1:8181/ >nul 2>&1 && set "GOV_OK=1"
curl -s -m 1 http://127.0.0.1:8183/harvest >nul 2>&1 && set "HACK_OK=1"
if "!QS_OK!!GOV_OK!!HACK_OK!"=="111" goto ALL_READY
if !READY_TRY! GEQ 90 (
    echo [ERROR] Services not ready after ~90s  qs=!QS_OK! gov=!GOV_OK! hacker=!HACK_OK!
    goto CLEANUP
)
echo       ... qs=!QS_OK! gov=!GOV_OK! hacker=!HACK_OK! [!READY_TRY!/90]
ping -n 2 127.0.0.1 >nul
goto WAIT_ALL

:ALL_READY
echo       [OK] Quantum :8184  Gov :8181  Hacker :8183

if "!SKIP_TESTS!"=="1" goto OPEN_BROWSERS

echo [4/5] Opening 4-panel Chrome UI (2x2) + Selenium demo...
start "" "http://127.0.0.1:8181"
start "" "http://127.0.0.1:8181/officer/review"
start "" "http://127.0.0.1:8183/dashboard"
start "" "http://127.0.0.1:8183/harvest"
start "" "http://127.0.0.1:8183/decrypt"
cd /d "%DEMO_DIR%ui-tests"
REM ComprehensiveCryptoTest opens 4 visible Chrome panels and completes (does not hang forever)
call mvn test "-Dtest=com.pqc.selenium.ComprehensiveCryptoTest"
set TEST_RESULT=!errorlevel!
cd /d "%DEMO_DIR%"
if !TEST_RESULT! equ 0 (echo       [OK] Selenium 4-panel demo passed) else (echo       [WARN] Selenium finished with issues)

:OPEN_BROWSERS
echo [5/5] Opening browser panels...
start "" "http://127.0.0.1:8181"
start "" "http://127.0.0.1:8181/officer/review"
start "" "http://127.0.0.1:8183/dashboard"
start "" "http://127.0.0.1:8183/decrypt"

echo.
echo =============================================================================================
echo   RUNNING  ^|  john.citizen / Citizen@2024!  ^|  officer / Officer@2024!
echo   Gov http://127.0.0.1:8181  Hacker http://127.0.0.1:8183  Quantum http://127.0.0.1:8184
echo =============================================================================================
echo.

if "!KEEP_MENU!"=="1" goto MENU_LOOP
echo Services left RUNNING. Close windows or run: run-demo.bat cleanup
exit /b 0

:MENU_LOOP
echo [O] Open panels  [T] Re-test  [Q] Quit
choice /C OTQ /N /M "Select: "
if errorlevel 3 goto CLEANUP
if errorlevel 2 (
    cd /d "%DEMO_DIR%ui-tests"
    call mvn -q test "-Dtest=com.pqc.selenium.ComprehensiveCryptoTest"
    cd /d "%DEMO_DIR%"
    goto MENU_LOOP
)
if errorlevel 1 (
    start "" "http://127.0.0.1:8181"
    start "" "http://127.0.0.1:8183/dashboard"
    start "" "http://127.0.0.1:8183/decrypt"
    goto MENU_LOOP
)
goto MENU_LOOP

REM ═══════════════════════════════════════════════════════════════════════════════
REM DOCKER MODE (gov/messaging in compose + local quantum + local hacker)
REM ═══════════════════════════════════════════════════════════════════════════════
:DOCKER_MODE
where docker >nul 2>&1 || (echo [ERROR] Docker not on PATH & exit /b 1)
docker info >nul 2>&1 || (echo [ERROR] Docker Desktop not running & exit /b 1)

echo [0/4] Prerequisites OK
for /d %%D in ("C:\Program Files\Java\jdk-25*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D" & set "PATH=%%D\bin;!PATH!"
if exist "C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3\include\cuda.h" (
    set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3"
    set "CUDA_HOME=!CUDA_PATH!"
    set "PATH=!CUDA_PATH!\bin;!PATH!"
)

if not exist "%DEMO_DIR%hacker-console\target\hacker-console-1.0.1.jar" (
    echo       Building jars for local hacker/quantum...
    cd /d "%DEMO_DIR%"
    call mvn -q -DskipTests package
    if errorlevel 1 exit /b 1
)

echo [1/4] Stopping old stack / freeing ports...
pushd "%DEMO_DIR%"
docker compose down >nul 2>&1
popd
for %%P in (8181 8182 8183 8184) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
)

echo [2/4] docker compose up -d (parallel with local quantum+hacker)...
pushd "%DEMO_DIR%"
start /b cmd /c "docker compose up -d --build"
popd
start "Quantum Simulator" /min "%DEMO_DIR%quantum-simulator\start-quantum-fast.bat"
start "Hacker Console" /min java -jar "%DEMO_DIR%hacker-console\target\hacker-console-1.0.1.jar" --spring.profiles.active=standalone

echo [3/4] Fast-polling readiness...
set /a READY_TRY=0
:WAIT_DOCKER_ALL
set /a READY_TRY+=1
set "QS_OK=0" & set "GOV_OK=0" & set "HACK_OK=0"
curl -s -m 1 http://127.0.0.1:8184/api/quantum/status >nul 2>&1 && set "QS_OK=1"
curl -s -m 1 http://127.0.0.1:8181/ >nul 2>&1 && set "GOV_OK=1"
curl -s -m 1 http://127.0.0.1:8183/harvest >nul 2>&1 && set "HACK_OK=1"
if "!QS_OK!!GOV_OK!!HACK_OK!"=="111" goto DOCKER_READY
if !READY_TRY! GEQ 120 (
    echo [ERROR] Docker stack not ready qs=!QS_OK! gov=!GOV_OK! hacker=!HACK_OK!
    goto DOCKER_CLEANUP
)
echo       ... qs=!QS_OK! gov=!GOV_OK! hacker=!HACK_OK! [!READY_TRY!/120]
ping -n 2 127.0.0.1 >nul
goto WAIT_DOCKER_ALL

:DOCKER_READY
echo       [OK] All services ready

if "!SKIP_TESTS!"=="0" (
    echo [4/4] Selenium...
    cd /d "%DEMO_DIR%ui-tests"
    call mvn -q test "-Dtest=com.pqc.selenium.ComprehensiveCryptoTest"
    cd /d "%DEMO_DIR%"
)

start "" "http://127.0.0.1:8181"
start "" "http://127.0.0.1:8183/dashboard"
start "" "http://127.0.0.1:8183/decrypt"

echo.
echo =============================================================================================
echo   DOCKER DEMO RUNNING  ^|  services left UP
echo =============================================================================================
if "!KEEP_MENU!"=="1" goto DOCKER_MENU
exit /b 0

:DOCKER_MENU
echo [O] Open  [L] Logs  [Q] Quit
choice /C OLQ /N /M "Select: "
if errorlevel 3 goto DOCKER_CLEANUP
if errorlevel 2 (docker compose -f "%DEMO_DIR%docker-compose.yml" logs --tail=40 & goto DOCKER_MENU)
if errorlevel 1 (
    start "" "http://127.0.0.1:8181"
    start "" "http://127.0.0.1:8183/decrypt"
    goto DOCKER_MENU
)
goto DOCKER_MENU

:DOCKER_CLEANUP
echo Stopping Docker + local processes...
pushd "%DEMO_DIR%"
docker compose down
popd
taskkill /F /FI "WINDOWTITLE eq Quantum Simulator*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Hacker Console*" >nul 2>&1
for %%P in (8181 8182 8183 8184) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
)
echo [OK] Cleaned up.
exit /b 0

:CLEANUP
echo Cleaning up EXE services...
taskkill /F /FI "WINDOWTITLE eq Gov-Portal*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Hacker Console*" >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Quantum Simulator*" >nul 2>&1
taskkill /F /IM java.exe >nul 2>&1
for %%P in (8181 8182 8183 8184) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P.*LISTENING"') do taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=2" %%a in ('wmic process where "commandline like '%%quantum_service%%'" get processid 2^>nul ^| findstr /r "[0-9]"') do taskkill /F /PID %%a >nul 2>&1
echo [OK] Cleaned up.
exit /b 0
