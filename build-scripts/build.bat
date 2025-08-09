@echo off
setlocal enabledelayedexpansion

:: Build Script for Financial Spring Boot Application
:: Author: Generated for Financial Project
:: Version: 1.0

echo ========================================
echo Financial Application Build Script
echo ========================================

:: Set default values
set BUILD_TYPE=build
set PROFILE=debug
set DOCKER_BUILD=false
set SKIP_TESTS=false
set CLEAN_BUILD=false

:: Parse command line arguments
:parse_args
if "%~1"=="" goto :main
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
if /i "%~1"=="--clean" set CLEAN_BUILD=true
if /i "%~1"=="--docker" set DOCKER_BUILD=true
if /i "%~1"=="--skip-tests" set SKIP_TESTS=true
if /i "%~1"=="--profile" (
    set PROFILE=%~2
    shift
)
if /i "%~1"=="--type" (
    set BUILD_TYPE=%~2
    shift
)
shift
goto :parse_args

:show_help
echo.
echo Usage: build.bat [OPTIONS]
echo.
echo Options:
echo   --help, -h           Show this help message
echo   --clean              Clean build (removes build artifacts)
echo   --docker             Build Docker image after successful build
echo   --skip-tests         Skip running tests
echo   --profile PROFILE    Set Spring profile (debug^|test^|release) [default: debug]
echo   --type TYPE          Build type (build^|test^|package^|run) [default: build]
echo.
echo Examples:
echo   build.bat --clean --docker
echo   build.bat --profile release --skip-tests
echo   build.bat --type test
echo.
goto :end

:main
echo.
echo Build Configuration:
echo   Build Type: %BUILD_TYPE%
echo   Profile: %PROFILE%
echo   Clean Build: %CLEAN_BUILD%
echo   Docker Build: %DOCKER_BUILD%
echo   Skip Tests: %SKIP_TESTS%
echo.

:: Check if Gradle wrapper exists in parent directory
if not exist "..\gradlew.bat" (
    echo ERROR: Gradle wrapper not found. Please ensure gradlew.bat exists in the project root.
    exit /b 1
)

:: Clean build if requested
if "%CLEAN_BUILD%"=="true" (
    echo [INFO] Cleaning build artifacts...
    call ..\gradlew.bat clean
    if errorlevel 1 (
        echo ERROR: Clean failed
        exit /b 1
    )
    echo [INFO] Clean completed successfully
    echo.
)

:: Set Spring profile
set SPRING_PROFILES_ACTIVE=%PROFILE%

:: Execute build based on type
if "%BUILD_TYPE%"=="test" goto :run_tests
if "%BUILD_TYPE%"=="package" goto :package_app
if "%BUILD_TYPE%"=="run" goto :run_app
if "%BUILD_TYPE%"=="build" goto :build_app

echo ERROR: Invalid build type '%BUILD_TYPE%'
goto :show_help

:build_app
echo [INFO] Building application with profile: %PROFILE%
call ..\gradlew.bat build -Dspring.profiles.active=%PROFILE%
if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)
echo [INFO] Build completed successfully
goto :post_build

:run_tests
echo [INFO] Running tests with profile: %PROFILE%
if "%SKIP_TESTS%"=="true" (
    echo [INFO] Tests skipped as requested
) else (
    call ..\gradlew.bat test -Dspring.profiles.active=%PROFILE%
    if errorlevel 1 (
        echo ERROR: Tests failed
        exit /b 1
    )
    echo [INFO] Tests completed successfully
)
goto :end

:package_app
echo [INFO] Packaging application with profile: %PROFILE%
call ..\gradlew.bat bootJar -Dspring.profiles.active=%PROFILE%
if errorlevel 1 (
    echo ERROR: Packaging failed
    exit /b 1
)
echo [INFO] Application packaged successfully
goto :post_build

:run_app
echo [INFO] Running application with profile: %PROFILE%
call ..\gradlew.bat bootRun -Dspring.profiles.active=%PROFILE%
goto :end

:post_build
:: Build Docker image if requested
if "%DOCKER_BUILD%"=="true" (
    echo.
    echo [INFO] Building Docker image...
    
    :: Check if Docker is available
    docker --version >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Docker is not installed or not in PATH
        exit /b 1
    )
    
    :: Copy JAR to Docker directory (parent directory)
    if exist "..\build\libs\financial-0.0.1-SNAPSHOT.jar" (
        copy "..\build\libs\financial-0.0.1-SNAPSHOT.jar" "..\financial-0.0.1-SNAPSHOT.jar"
        echo [INFO] JAR file copied for Docker build
    ) else (
        echo ERROR: JAR file not found in build/libs/
        exit /b 1
    )
    
    :: Build Docker image from parent directory
    cd ..
    docker build -t pars/financial:latest .
    if errorlevel 1 (
        echo ERROR: Docker build failed
        exit /b 1
    )
    
    :: Clean up copied JAR
    if exist "financial-0.0.1-SNAPSHOT.jar" del "financial-0.0.1-SNAPSHOT.jar"
    cd build-scripts
    
    echo [INFO] Docker image built successfully: pars/financial:latest
)

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo Next steps:
echo   - Run tests: build.bat --type test
echo   - Package app: build.bat --type package
echo   - Run app: build.bat --type run
echo   - Build with Docker: build.bat --docker
echo   - Clean build: build.bat --clean
echo.

:end
endlocal
