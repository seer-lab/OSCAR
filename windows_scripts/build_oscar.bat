@echo off

mvn -f pom.xml compile > NUL 2>&1 && echo Successfully Built OSCAR || echo Failed to build OSCAR