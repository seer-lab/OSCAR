package oscar.utils;

import org.apache.commons.cli.*;

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
  }

  public static void parse(String[] args) throws ParseException {
    commandLine = parser.parse(options, args);

    PropertiesFile = getOptionValue("p");
  }

  private static String getOptionValue(String option) {
    if (!commandLine.hasOption(option) || commandLine.getOptionValue(option) == null) {
      String optionDescription = options.getOption(option).getDescription();
      throw new RuntimeException("Expected '%s' for required option '%s'".formatted(optionDescription, option));
    }

    return commandLine.getOptionValue(option);
  }
}
