package oscar.utils;

import org.apache.commons.cli.*;
import oscar.utils.logger.LoggerFactory;

import java.util.logging.Level;

/**
 * This class is in charge of defining and parsing the CLI
 */
public final class OptionsParser {
  private static final Options options = new Options();
  private static final CommandLineParser parser = new DefaultParser();
  private static CommandLine commandLine;

  // Define options
  public static String PropertiesFile;

  static {
    options.addOption("p", true, "properties file location");
    options.addOption("v", false, "verbose output");
    options.addOption("q", false, "quiet output");
  }

  public static void parse(String[] args) throws ParseException {
    commandLine = parser.parse(options, args);

    PropertiesFile = getOptionValue("p");

    // Initialize Logger
    if (commandLine.hasOption("v"))
      LoggerFactory.setLevel(Level.ALL);

    if (commandLine.hasOption("q"))
      LoggerFactory.setLevel(Level.OFF);
  }

  private static String getOptionValue(String option) {
    if (!commandLine.hasOption(option) || commandLine.getOptionValue(option) == null) {
      String optionDescription = options.getOption(option).getDescription();
      throw new RuntimeException("Expected '" + optionDescription + "' for required option '" + option + "'");
    }

    return commandLine.getOptionValue(option);
  }
}
