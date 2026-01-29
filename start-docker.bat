@echo off
echo ========================================
echo Issue Tracker - Quick Docker Start
echo ========================================
echo.

echo Building application...
REM Try mvn from PATH first, then try full path
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    call mvn clean package -DskipTests
) else (
    if exist "C:\apache-maven-3.9.12\bin\mvn.cmd" (
        call "C:\apache-maven-3.9.12\bin\mvn.cmd" clean package -DskipTests
    ) else (
        echo ERROR: Maven not found in PATH or at C:\apache-maven-3.9.12\bin\mvn.cmd
        echo Please install Maven or update the path in this script
        pause
        exit /b 1
    )
)

if %errorlevel% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Checking Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running or not installed
    echo Please start Docker Desktop and try again
    pause
    exit /b 1
)
echo Docker is running

echo.
echo Starting Docker services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ERROR: Failed to start Docker services
    echo.
    echo Troubleshooting:
    echo 1. Make sure Docker Desktop is running
    echo 2. Check if ports 8080 and 27017 are available
    echo 3. Try: docker-compose down
    echo 4. Then run this script again
    pause
    exit /b 1
)

echo.
echo ========================================
echo Services started successfully!
echo ========================================
echo.
echo Application: http://localhost:8080
echo Swagger UI:  http://localhost:8080/swagger-ui.html
echo.
echo View logs with: docker-compose logs -f
echo Stop with:      docker-compose down
echo.
pause
