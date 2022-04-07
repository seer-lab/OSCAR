# OSCAR Noise Injector - Prototype

### Requirements (Tested on)
- Apache Maven 3.6.3
- OpenJDK 17.0.2 (Project compiles to Java 9)

### Running Instructions

```sh
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass=oscar.Main -Dexec.args="-h"
```

##### OSCAR arguments

```
oscar <targetfile> <mainclass> <outputdirectory>
```

##### Instrumented program arguments

```
Usage:
        java [java_options] <mainclass> [oscar_options]
                (to execute a class)
        or: java -jar <mainclass> [oscar_options]
                (to execute a jar file)

OSCAR options include:
        --args -a                     Inject arguments into the program
        --config_file -c              Set config file location to load
        --output -o                   Set output file location
        --max_sleep_length -M         Set maximum sleep length
        --min_sleep_length -m         Set minimum sleep length
        --disable-noise -d            Disable all noise
        --verbose -v                  Enable full logging.
        --quiet -q                    Disable logging.
        --help -h                     Print Help.

OSCAR Noise Injector 2022
```
###### Note:

- Sleep lengths should be integers
- Arguments need to be inside quotes as such: "-a -b -c"