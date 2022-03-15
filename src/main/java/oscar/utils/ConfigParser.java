package oscar.utils;

import oscar.utils.error.CustomRuntimeException;
import oscar.utils.logger.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ConfigParser {
  private static Logger logger;
  private static Properties props;

  public static String TargetFile;
  public static String OutputDirectory;
  public static String TargetDirectory;
  public static String MainClass;

  /**
   * Parse the properties file with program instructions
   *
   * @param propertiesFile the properties file location
   */
  public static void parse(String propertiesFile) {
    // Try to fetch the properties file
    try {
      props = new Properties();
      props.load(new FileInputStream(propertiesFile));
    } catch (IOException e) {
      System.err.printf("Failed to find client.properties file in '%s'.", propertiesFile);
      System.exit(1);
    }

    // Get logger level and initialize logger factory
    String logLevel = getPropertyWithSetValues(
        "log_level",
        "ALL",
        List.of("OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL")
    );

    LoggerFactory.initialize(Level.parse(logLevel));
    logger = LoggerFactory.getInstance(ConfigParser.class);

    // Get required properties
    TargetFile = getRequiredProperty("target_file");
    TargetDirectory = Path.of(TargetFile).getParent().toString();
    OutputDirectory = getRequiredProperty("output_dir");
    MainClass = getRequiredProperty("main_class");
  }

  private static String getRequiredProperty(String property) {
    String value = props.getProperty(property);

    if (value == null)
      throw new CustomRuntimeException(logger, "Required property '%s' not set".formatted(property));

    return value;
  }

  private static String getPropertyWithSetValues(String property, String defaultValue, List<String> allowedValues) {
    String value = props.getProperty(property, defaultValue);

    if (!allowedValues.contains(value))
      throw new CustomRuntimeException(logger, "Invalid value '%s' for property '%s'.".formatted(value, property));

    return value;
  }
}