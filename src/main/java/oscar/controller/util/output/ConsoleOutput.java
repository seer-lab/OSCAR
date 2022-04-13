package oscar.controller.util.output;

import oscar.controller.Controller;
import oscar.utils.logger.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleOutput implements ControllerOutput {
  private static final Logger logger = LoggerFactory.getInstance(ConsoleOutput.class);

  public ConsoleOutput() {
    logger.setLevel(Level.ALL);
  }

  @Override
  public void write(String output) {
    logger.info(output);
  }

  @Override
  public void terminate() {
  }
}
