#!/bin/bash

# Build Script for Financial Spring Boot Application
# Author: Generated for Financial Project
# Version: 1.0

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show help
show_help() {
    echo
    echo "Usage: ./build.sh [OPTIONS]"
    echo
    echo "Options:"
    echo "  --help, -h           Show this help message"
    echo "  --clean              Clean build (removes build artifacts)"
    echo "  --docker             Build Docker image after successful build"
    echo "  --skip-tests         Skip running tests"
    echo "  --profile PROFILE    Set Spring profile (debug|test|release) [default: debug]"
    echo "  --type TYPE          Build type (build|test|package|run) [default: build]"
    echo
    echo "Examples:"
    echo "  ./build.sh --clean --docker"
    echo "  ./build.sh --profile release --skip-tests"
    echo "  ./build.sh --type test"
    echo
}

# Function to check prerequisites
check_prerequisites() {
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check Java version (requires Java 21)
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        print_error "Java 21 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    # Check if Gradle wrapper exists in parent directory
    if [ ! -f "../gradlew" ]; then
        print_error "Gradle wrapper not found. Please ensure gradlew exists in the project root."
        exit 1
    fi
    
    # Make gradlew executable
    chmod +x ../gradlew
    
    print_success "Prerequisites check passed"
}

# Function to build Docker image
build_docker() {
    print_info "Building Docker image..."
    
    # Check if Docker is available
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    # Copy JAR to Docker directory (parent directory)
    if [ -f "../build/libs/financial-0.0.1-SNAPSHOT.jar" ]; then
        cp ../build/libs/financial-0.0.1-SNAPSHOT.jar ../financial-0.0.1-SNAPSHOT.jar
        print_info "JAR file copied for Docker build"
    else
        print_error "JAR file not found in build/libs/"
        exit 1
    fi
    
    # Build Docker image from parent directory
    cd ..
    docker build -t pars/financial:latest .
    if [ $? -ne 0 ]; then
        print_error "Docker build failed"
        cd build-scripts
        exit 1
    fi
    cd build-scripts
    
    # Clean up copied JAR
    rm -f ../financial-0.0.1-SNAPSHOT.jar
    
    print_success "Docker image built successfully: pars/financial:latest"
}

# Main script
main() {
    echo "========================================"
    echo "Financial Application Build Script"
    echo "========================================"
    
    # Set default values
    BUILD_TYPE="build"
    PROFILE="debug"
    DOCKER_BUILD=false
    SKIP_TESTS=false
    CLEAN_BUILD=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help|-h)
                show_help
                exit 0
                ;;
            --clean)
                CLEAN_BUILD=true
                shift
                ;;
            --docker)
                DOCKER_BUILD=true
                shift
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --profile)
                PROFILE="$2"
                shift 2
                ;;
            --type)
                BUILD_TYPE="$2"
                shift 2
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    echo
    echo "Build Configuration:"
    echo "  Build Type: $BUILD_TYPE"
    echo "  Profile: $PROFILE"
    echo "  Clean Build: $CLEAN_BUILD"
    echo "  Docker Build: $DOCKER_BUILD"
    echo "  Skip Tests: $SKIP_TESTS"
    echo
    
    # Check prerequisites
    check_prerequisites
    
    # Clean build if requested
    if [ "$CLEAN_BUILD" = true ]; then
        print_info "Cleaning build artifacts..."
        ../gradlew clean
        if [ $? -ne 0 ]; then
            print_error "Clean failed"
            exit 1
        fi
        print_success "Clean completed successfully"
        echo
    fi
    
    # Set Spring profile
    export SPRING_PROFILES_ACTIVE="$PROFILE"
    
    # Execute build based on type
    case $BUILD_TYPE in
        "test")
            print_info "Running tests with profile: $PROFILE"
            if [ "$SKIP_TESTS" = true ]; then
                print_info "Tests skipped as requested"
            else
                ../gradlew test -Dspring.profiles.active="$PROFILE"
                if [ $? -ne 0 ]; then
                    print_error "Tests failed"
                    exit 1
                fi
                print_success "Tests completed successfully"
            fi
            ;;
        "package")
            print_info "Packaging application with profile: $PROFILE"
            ../gradlew bootJar -Dspring.profiles.active="$PROFILE"
            if [ $? -ne 0 ]; then
                print_error "Packaging failed"
                exit 1
            fi
            print_success "Application packaged successfully"
            ;;
        "run")
            print_info "Running application with profile: $PROFILE"
            ../gradlew bootRun -Dspring.profiles.active="$PROFILE"
            ;;
        "build")
            print_info "Building application with profile: $PROFILE"
            ../gradlew build -Dspring.profiles.active="$PROFILE"
            if [ $? -ne 0 ]; then
                print_error "Build failed"
                exit 1
            fi
            print_success "Build completed successfully"
            ;;
        *)
            print_error "Invalid build type: $BUILD_TYPE"
            show_help
            exit 1
            ;;
    esac
    
    # Build Docker image if requested and not just running tests
    if [ "$DOCKER_BUILD" = true ] && [ "$BUILD_TYPE" != "test" ] && [ "$BUILD_TYPE" != "run" ]; then
        build_docker
    fi
    
    echo
    echo "========================================"
    print_success "Build completed successfully!"
    echo "========================================"
    echo
    echo "Next steps:"
    echo "  - Run tests: ./build.sh --type test"
    echo "  - Package app: ./build.sh --type package"
    echo "  - Run app: ./build.sh --type run"
    echo "  - Build with Docker: ./build.sh --docker"
    echo "  - Clean build: ./build.sh --clean"
    echo
}

# Run main function with all arguments
main "$@"
