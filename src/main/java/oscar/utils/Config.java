package oscar.utils;

import java.io.*;
import java.util.logging.Level;

public abstract class Config {
  /**
   * Parse the program arguments and extract parameters and configurations to be used in the program
   *
   * @param args the array of arguments entered into the program
   */
  public static void parse(String[] args) {
    // Check for valid number of arguments
    if (args.length != 1) {
      System.out.println("Invalid number of arguments. Use '--help.'");
      System.exit(1);
    }

    // Check if person requested help
    if (args[0].startsWith("--")) {
      if (args[0].equals("--help")) {
        System.out.println("""
                           Usage: oscar [PROPERTIES FILE]
                           OSCAR is a noise injection framework for Java concurrent software.
                           """);
        System.exit(0);
      } else {
        System.out.printf("""
                          oscar: unrecognized option '%s'
                          Try 'oscar --help' for more information.
                          %n""", args[0]);
        System.exit(1);
      }
    }

    // Try to fetch the properties file
    java.util.Properties props = new java.util.Properties();
    try {
      props.load(new FileInputStream(args[0]));
    } catch (IOException e) {
      System.err.printf("Failed to find client.properties file in '%s'.", args[0]);
      System.exit(1);
    }

    // Get logger level
    String logLevelString = props.getProperty("log_level", "ALL");

    try {
      Level logLevel = Level.parse(logLevelString);
      LoggerFactory.initialize(logLevel);
    } catch (IllegalArgumentException e) {
      invalidPropertyValueRead("log_level", logLevelString);
    }
  }

  private static void invalidPropertyValueRead(String propertyName, String propertyValue) {
    System.err.printf("Invalid value '%s' for property '%s'.", propertyValue, propertyName);
    System.exit(1);
  }
}