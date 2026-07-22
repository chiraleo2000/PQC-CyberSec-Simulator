@echo off
REM Quick service starter for demo

echo Starting Quantum Simulator...
start "Quantum Simulator" cmd /k "cd /d %~dp0quantum-simulator && if exist \"C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3\include\cuda.h\" (set CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3& set CUDA_HOME=%CUDA_PATH%& set PATH=%CUDA_PATH%\bin;%PATH%) && python quantum_service.py"
timeout /t 5 /nobreak >nul

echo Starting Gov-Portal...
start "Gov-Portal" cmd /k "cd /d %~dp0gov-portal && mvn spring-boot:run -Dspring-boot.run.profiles=h2"
timeout /t 5 /nobreak >nul

echo Starting Hacker Console...
start "Hacker Console" cmd /k "cd /d %~dp0hacker-console && mvn spring-boot:run -Dspring-boot.run.profiles=standalone"

echo.
echo All services starting in separate windows...
echo Wait 60 seconds for full startup, then run tests.
echo.
pause
