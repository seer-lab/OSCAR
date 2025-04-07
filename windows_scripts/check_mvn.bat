@echo off

mvn --version

if %ERRORLEVEL% NEQ 0 (
    echo Maven is not in your PATH. Please either add it or get it from https://maven.apache.org/install.html
    exit /b %ERRORLEVEL%
) else (
    echo Maven is in your path.
)
