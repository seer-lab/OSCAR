package oscar.controller.util;

import oscar.utils.logger.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class ControllerConfigFile {
  private static final Logger logger = LoggerFactory.getInstance(ControllerConfigFile.class);

  public static Long parseLong(Properties props, String property, String defaultValue) {
    try {
      return Long.parseLong(props.getProperty(property, defaultValue));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid property value for '" + property + "'.", e);
    }
  }

  public static Properties getConfigFile(String location) {

    try {
      Properties props = new Properties();
      props.load(new FileInputStream(location));
      return props;
    } catch (IOException e) {
      throw new RuntimeException("Failed to find client.properties file in '" + location + "'.", e);
    }
  }
}