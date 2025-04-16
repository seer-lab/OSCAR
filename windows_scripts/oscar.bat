@echo off

mvn -q -f . exec:java -Dexec.args="%*"