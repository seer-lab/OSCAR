@echo off

mvn --version > NUL 2>&1 && echo Maven is in your path. || echo Maven is not in your PATH. Please either add it or get it from https://maven.apache.org/install.html
