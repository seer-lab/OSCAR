@echo off

mvn -f ../pom.xml compile > NUL 2>&1

set exit_code=%ERRORLEVEL%
if %exit_code% EQ 0 (
    echo Successfully Built OSCAR
    exit /b %exit_code%
) else (
    echo Failed to build OSCAR with error code %exit_code%
)
