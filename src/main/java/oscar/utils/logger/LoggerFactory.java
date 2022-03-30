package oscar.utils.logger;

import java.util.logging.*;

public final class LoggerFactory {
  private static Level loggerLevel = Level.ALL;
  private static ConsoleHandler consoleHandler;
  private static FileHandler fileHandler;

  public static void initialize(Level level) {
    // Get level from properties and configure the handler with its properties
    loggerLevel = level;

    // Configure console handler
    consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(loggerLevel);
    consoleHandler.setFormatter(new LoggerFormatter());

    /*
    // Configure file handler
    try {
      fileHandler = new FileHandler("log.txt", false);
      fileHandler.setLevel(loggerLevel);
      fileHandler.setFormatter(new LogFormatter());
    } catch (IOException | SecurityException e) {
      throw new RuntimeException("Failed to write to log file location in client.properties file.", e);
    }
    */
  }

  public static void initialize() {
    initialize(Level.ALL);
  }

  public static Logger getInstance(Class<?> clazz) {
    Logger logger = Logger.getLogger(clazz.getName());
    logger.setUseParentHandlers(false);
    logger.setLevel(loggerLevel);

    logger.addHandler(consoleHandler);
    //logger.addHandler(fileHandler);

    return logger;
  }
}