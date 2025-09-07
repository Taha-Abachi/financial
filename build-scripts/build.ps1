# Build Script for Financial Spring Boot Application
# Author: Generated for Financial Project
# Version: 1.0

param(
    [string]$BuildType = "build",
    [string]$Profile = "debug",
    [switch]$DockerBuild,
    [switch]$SkipTests,
    [switch]$CleanBuild,
    [switch]$Help
)

# Function to write colored output
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Function to show help
function Show-Help {
    Write-Host ""
    Write-Host "Usage: .\build.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "Parameters:"
    Write-Host "  -BuildType TYPE     Build type (build|test|package|run) [default: build]"
    Write-Host "  -Profile PROFILE    Set Spring profile (debug|test|release) [default: debug]"
    Write-Host "  -DockerBuild        Build Docker image after successful build"
    Write-Host "  -SkipTests          Skip running tests"
    Write-Host "  -CleanBuild         Clean build (removes build artifacts)"
    Write-Host "  -Help               Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\build.ps1 -CleanBuild -DockerBuild"
    Write-Host "  .\build.ps1 -Profile release -SkipTests"
    Write-Host "  .\build.ps1 -BuildType test"
    Write-Host ""
}

# Function to check prerequisites
function Test-Prerequisites {
    # Check if Java is installed
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
        if ($javaVersion -match '"(\d+)\.') {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -lt 21) {
                Write-Error "Java 21 or higher is required. Current version: $majorVersion"
                exit 1
            }
        }
    }
    catch {
        Write-Error "Java is not installed or not in PATH"
        exit 1
    }
    
    # Check if Gradle wrapper exists in parent directory
    if (-not (Test-Path "..\gradlew.bat")) {
        Write-Error "Gradle wrapper not found. Please ensure gradlew.bat exists in the project root."
        exit 1
    }
    
    Write-Success "Prerequisites check passed"
}

# Function to build Docker image
function Build-DockerImage {
    Write-Info "Building Docker image..."
    
    # Check if Docker is available
    try {
        docker --version | Out-Null
    }
    catch {
        Write-Error "Docker is not installed or not in PATH"
        exit 1
    }
    
    # Copy JAR to Docker directory (parent directory)
    $jarPath = "..\build\libs\financial-0.0.1-SNAPSHOT.jar"
    if (Test-Path $jarPath) {
        Copy-Item $jarPath "..\financial-0.0.1-SNAPSHOT.jar"
        Write-Info "JAR file copied for Docker build"
    }
    else {
        Write-Error "JAR file not found in build/libs/"
        exit 1
    }
    
    # Build Docker image from parent directory
    Push-Location ..
    $dockerResult = docker build -t pars/financial:latest .
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Docker build failed"
        Pop-Location
        exit 1
    }
    Pop-Location
    
    # Clean up copied JAR
    if (Test-Path "..\financial-0.0.1-SNAPSHOT.jar") {
        Remove-Item "..\financial-0.0.1-SNAPSHOT.jar"
    }
    
    Write-Success "Docker image built successfully: pars/financial:latest"
}

# Main script
function Main {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Financial Application Build Script" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    # Show help if requested
    if ($Help) {
        Show-Help
        return
    }
    
    Write-Host ""
    Write-Host "Build Configuration:"
    Write-Host "  Build Type: $BuildType"
    Write-Host "  Profile: $Profile"
    Write-Host "  Clean Build: $CleanBuild"
    Write-Host "  Docker Build: $DockerBuild"
    Write-Host "  Skip Tests: $SkipTests"
    Write-Host ""
    
    # Check prerequisites
    Test-Prerequisites
    
    # Clean build if requested
    if ($CleanBuild) {
        Write-Info "Cleaning build artifacts..."
        $cleanResult = & ..\gradlew.bat clean
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Clean failed"
            exit 1
        }
        Write-Success "Clean completed successfully"
        Write-Host ""
    }
    
    # Set Spring profile
    $env:SPRING_PROFILES_ACTIVE = $Profile
    
    # Execute build based on type
    switch ($BuildType.ToLower()) {
        "test" {
            Write-Info "Running tests with profile: $Profile"
            if ($SkipTests) {
                Write-Info "Tests skipped as requested"
            }
            else {
                $testResult = & ..\gradlew.bat test "-Dspring.profiles.active=$Profile"
                if ($LASTEXITCODE -ne 0) {
                    Write-Error "Tests failed"
                    exit 1
                }
                Write-Success "Tests completed successfully"
            }
        }
        "package" {
            Write-Info "Packaging application with profile: $Profile"
            $packageResult = & ..\gradlew.bat bootJar "-Dspring.profiles.active=$Profile"
            if ($LASTEXITCODE -ne 0) {
                Write-Error "Packaging failed"
                exit 1
            }
            Write-Success "Application packaged successfully"
        }
        "run" {
            Write-Info "Running application with profile: $Profile"
            & ..\gradlew.bat bootRun "-Dspring.profiles.active=$Profile"
        }
        "build" {
            Write-Info "Building application with profile: $Profile"
            $buildResult = & ..\gradlew.bat build "-Dspring.profiles.active=$Profile"
            if ($LASTEXITCODE -ne 0) {
                Write-Error "Build failed"
                exit 1
            }
            Write-Success "Build completed successfully"
        }
        default {
            Write-Error "Invalid build type: $BuildType"
            Show-Help
            exit 1
        }
    }
    
    # Build Docker image if requested and not just running tests
    if ($DockerBuild -and $BuildType -ne "test" -and $BuildType -ne "run") {
        Build-DockerImage
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Success "Build completed successfully!"
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Next steps:"
    Write-Host "  - Run tests: .\build.ps1 -BuildType test"
    Write-Host "  - Package app: .\build.ps1 -BuildType package"
    Write-Host "  - Run app: .\build.ps1 -BuildType run"
    Write-Host "  - Build with Docker: .\build.ps1 -DockerBuild"
    Write-Host "  - Clean build: .\build.ps1 -CleanBuild"
    Write-Host ""
}

# Run main function
Main
