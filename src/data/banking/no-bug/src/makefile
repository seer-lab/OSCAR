.PHONY: all run-main compile clear

all: run-main clear

MAIN=Bank
run-main: compile
	@java $(MAIN)

compile:
	@javac *.java

clear:
	@rm *.class