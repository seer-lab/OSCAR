.PHONY: test run-test compile clear

JUNIT_JAR=../../../../lib/JUnit/junit-4.12.jar
HAMCREST_JAR=../../../../lib/Hamcrest/hamcrest-core-1.3.jar
CLASSP=./$(JUNIT_JAR):./$(HAMCREST_JAR):../src/:.

test: run-test clear

TESTS_MAIN=RunTests
run-test: compile
	@java -cp $(CLASSP) $(TESTS_MAIN)

compile:
	@javac -cp $(CLASSP) *.java

clear:
	@rm *.class
	@rm ../src/*.class