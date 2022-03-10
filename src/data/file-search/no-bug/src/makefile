.PHONY: all run compile clean

all: run clean

MAIN=Search
run: compile
	# @python3 generate-test-files.py
	@java $(MAIN)

compile:
	@javac *.java

clean:
	@trash *.class