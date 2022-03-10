.PHONY: run-main run-test compile clear

JUNIT_JAR=../../../../lib/JUnit/junit-4.12.jar
HAMCREST_JAR=../../../../lib/Hamcrest/hamcrest-core-1.3.jar
CLASSP=./$(JUNIT_JAR):./$(HAMCREST_JAR):../src/:.

all: run-main clear
test: run-test clear

TESTS_MAIN=RunTests
run-test: compile
	@java -cp $(CLASSP) $(TESTS_MAIN)

MAIN=Main
run-main: compile
	@java -cp $(CLASSP) $(MAIN)

compile:
	@javac -cp $(CLASSP) *.java

clear:
	@rm *.class