# Build Scripts Documentation

This document describes the build scripts available for the Financial Spring Boot application.

## Overview

The project includes three build scripts for different platforms:

- **`build.bat`** - Windows Command Prompt batch script
- **`build.ps1`** - Windows PowerShell script (recommended for Windows)
- **`build.sh`** - Unix/Linux/macOS shell script

## Prerequisites

Before using the build scripts, ensure you have the following installed:

- **Java 21 or higher** - Required for Spring Boot 3.x
- **Gradle** - The project uses Gradle wrapper, so no separate installation needed
- **Docker** - Optional, only required if building Docker images

## Quick Start

### Windows (PowerShell - Recommended)
```powershell
# Basic build
.\build-scripts\build.ps1

# Build with Docker image
.\build-scripts\build.ps1 -DockerBuild

# Clean build
.\build-scripts\build.ps1 -CleanBuild

# Run tests only
.\build-scripts\build.ps1 -BuildType test
```

### Windows (Command Prompt)
```cmd
# Basic build
build-scripts\build.bat

# Build with Docker image
build-scripts\build.bat --docker

# Clean build
build-scripts\build.bat --clean

# Run tests only
build-scripts\build.bat --type test
```

### Linux/macOS
```bash
# Make script executable (first time only)
chmod +x build-scripts/build.sh

# Basic build
./build-scripts/build.sh

# Build with Docker image
./build-scripts/build.sh --docker

# Clean build
./build-scripts/build.sh --clean

# Run tests only
./build-scripts/build.sh --type test
```

## Build Types

The scripts support the following build types:

| Type | Description |
|------|-------------|
| `build` | Full build including tests (default) |
| `test` | Run tests only |
| `package` | Create executable JAR file |
| `run` | Build and run the application |

## Spring Profiles

The application supports different Spring profiles for different environments:

| Profile | Description | Default |
|---------|-------------|---------|
| `debug` | Development environment | ✓ |
| `test` | Testing environment | |
| `release` | Production environment | |

## Command Line Options

### PowerShell Script (`build.ps1`)

```powershell
.\build.ps1 [Parameters]

Parameters:
  -BuildType TYPE     Build type (build|test|package|run) [default: build]
  -Profile PROFILE    Set Spring profile (debug|test|release) [default: debug]
  -DockerBuild        Build Docker image after successful build
  -SkipTests          Skip running tests
  -CleanBuild         Clean build (removes build artifacts)
  -Help               Show help message
```

### Batch Script (`build.bat`)

```cmd
build.bat [OPTIONS]

Options:
  --help, -h           Show help message
  --clean              Clean build (removes build artifacts)
  --docker             Build Docker image after successful build
  --skip-tests         Skip running tests
  --profile PROFILE    Set Spring profile (debug|test|release) [default: debug]
  --type TYPE          Build type (build|test|package|run) [default: build]
```

### Shell Script (`build.sh`)

```bash
./build.sh [OPTIONS]

Options:
  --help, -h           Show help message
  --clean              Clean build (removes build artifacts)
  --docker             Build Docker image after successful build
  --skip-tests         Skip running tests
  --profile PROFILE    Set Spring profile (debug|test|release) [default: debug]
  --type TYPE          Build type (build|test|package|run) [default: build]
```

## Common Use Cases

### Development Workflow

1. **Clean build for fresh start:**
   ```powershell
   .\build-scripts\build.ps1 -CleanBuild
   ```

2. **Run tests only:**
   ```powershell
   .\build-scripts\build.ps1 -BuildType test
   ```

3. **Build and run application:**
   ```powershell
   .\build-scripts\build.ps1 -BuildType run
   ```

### Production Build

1. **Build for production with Docker:**
   ```powershell
   .\build-scripts\build.ps1 -Profile release -DockerBuild
   ```

2. **Package for deployment:**
   ```powershell
   .\build-scripts\build.ps1 -BuildType package -Profile release
   ```

### Testing

1. **Run tests with test profile:**
   ```powershell
   .\build-scripts\build.ps1 -BuildType test -Profile test
   ```

2. **Skip tests for quick build:**
   ```powershell
   .\build-scripts\build.ps1 -SkipTests
   ```

## Docker Integration

When using the `--docker` or `-DockerBuild` option, the script will:

1. Build the application JAR file
2. Copy the JAR to the project root
3. Build a Docker image with tag `pars/financial:latest`
4. Clean up the temporary JAR file

The Docker image can then be used with the existing `docker-compose.yaml` file.

## Error Handling

The scripts include comprehensive error handling:

- **Prerequisites checking** - Verifies Java version and Gradle wrapper
- **Build failure detection** - Exits with error code on build failures
- **Docker availability** - Checks if Docker is installed when needed
- **File existence** - Validates required files before operations

## Output

The scripts provide colored output for better readability:

- **Blue** - Information messages
- **Green** - Success messages
- **Yellow** - Warning messages
- **Red** - Error messages

## Troubleshooting

### Common Issues

1. **Java version error:**
   - Ensure Java 21+ is installed and in PATH
   - Check with: `java -version`

2. **Gradle wrapper not found:**
   - Ensure you're in the project root directory
   - Verify `gradlew.bat` (Windows) or `gradlew` (Unix) exists

3. **Docker build fails:**
   - Ensure Docker is installed and running
   - Check Docker daemon status

4. **Permission denied (Unix):**
   - Make script executable: `chmod +x build.sh`

### Getting Help

All scripts include help functionality:

```powershell
# PowerShell
.\build-scripts\build.ps1 -Help

# Batch
build-scripts\build.bat --help

# Shell
./build-scripts/build.sh --help
```

## Integration with CI/CD

These scripts can be easily integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions step
- name: Build Application
  run: |
    chmod +x build-scripts/build.sh
    ./build-scripts/build.sh --profile test --type test
```

```yaml
# Example GitLab CI step
build:
  script:
    - chmod +x build-scripts/build.sh
    - ./build-scripts/build.sh --profile release --docker
```

## File Structure

After a successful build, the following files will be created:

```
build/
├── libs/
│   └── financial-0.0.1-SNAPSHOT.jar    # Executable JAR
├── reports/
│   └── tests/                          # Test reports
└── classes/                            # Compiled classes
```

## Environment Variables

The scripts set the following environment variables:

- `SPRING_PROFILES_ACTIVE` - Set to the specified profile
- `JAVA_OPTS` - Can be set externally for JVM options

## Contributing

When modifying the build scripts:

1. Update all three scripts to maintain consistency
2. Test on the target platform
3. Update this documentation
4. Follow the existing code style and error handling patterns
