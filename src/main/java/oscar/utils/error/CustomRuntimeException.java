package oscar.utils.error;

import java.util.logging.Logger;

public final class CustomRuntimeException extends RuntimeException {
  public CustomRuntimeException(Logger logger, String text) {
    super(log(logger, text));
  }

  private static String log(Logger logger, String text) {
    logger.severe(text);

    return text;
  }
}