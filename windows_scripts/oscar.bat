@echo off

mvn -q -f ../ exec:java -Dexec.mainClass=oscar.Main -Dexec.args="%*"