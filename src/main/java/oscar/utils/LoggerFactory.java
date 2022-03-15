package oscar.utils;

import java.io.IOException;
import java.util.Date;
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
    consoleHandler.setFormatter(new LogFormatter());

    // Configure file handler
    try {
      fileHandler = new FileHandler("log.txt", false);
      fileHandler.setLevel(loggerLevel);
      fileHandler.setFormatter(new LogFormatter());
    } catch (IOException | SecurityException e) {
      throw new RuntimeException("Failed to write to log file location in client.properties file.");
    }
  }

  public static Logger getInstance(Class<?> clazz) {
    Logger logger = Logger.getLogger(clazz.getName());
    logger.setUseParentHandlers(false);
    logger.setLevel(loggerLevel);

    logger.addHandler(consoleHandler);
    logger.addHandler(fileHandler);

    return logger;
  }

  private static final class LogFormatter extends Formatter {
    private static final String format = "[%1$tF %1$tT][%2$s][%3$s]: %4$s %n";

    @Override
    public synchronized String format(LogRecord lr) {
      return String.format(format,
                           new Date(lr.getMillis()),
                           lr.getLoggerName(),
                           lr.getLevel().getLocalizedName(),
                           lr.getMessage()
      );
    }
  }
}