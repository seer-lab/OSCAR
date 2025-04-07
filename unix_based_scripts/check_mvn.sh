#!/bin/bash

# Run your command
mvn --version > /dev/null

# Check the exit code
if [ $? -ne 0 ]; then
    echo "Maven is not in your PATH. Please either add it or get it from https://maven.apache.org/install.html"
    exit $?
else
    echo "Maven is in your path."
fi
