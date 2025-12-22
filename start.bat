@echo off
REM PQC CyberSec Simulator - Start Script for Windows

echo.
echo ========================================
echo   PQC CyberSec Simulator - Startup
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo Docker is running. Starting services...
echo.

REM Copy env file if not exists
if not exist .env (
    echo Creating .env file from template...
    copy .env.example .env
)

REM Build and start containers
echo Building and starting Docker containers...
docker-compose up -d --build

echo.
echo ========================================
echo   Services Starting...
echo ========================================
echo.
echo Please wait 30-60 seconds for services to initialize.
echo.
echo Access the application at:
echo.
echo   User Service:     http://localhost:8081
echo   Messaging:        http://localhost:8082
echo   Hacker Simulation: http://localhost:8083
echo   pgAdmin:          http://localhost:5050
echo.
echo Default Admin Login:
echo   Username: admin
echo   Password: Admin@PQC2024!
echo.
echo ========================================

REM Wait for services
echo Waiting for services to start...
timeout /t 30 /nobreak >nul

REM Show status
docker-compose ps

echo.
echo To view logs: docker-compose logs -f
echo To stop: docker-compose down
echo.

pause
