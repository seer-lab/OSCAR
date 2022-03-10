.PHONY: all run-main compile clear

all: run-main clear

MAIN=Main
run-main: compile
	@java $(MAIN)

compile:
	@javac *.java

clear:
	@rm *.class