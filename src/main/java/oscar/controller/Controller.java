package oscar.controller;

import oscar.controller.noise.InstrumentedLocation;
import oscar.controller.util.ControllerOptions;
import oscar.utils.logger.LoggerFactory;

import java.util.*;
import java.util.logging.Logger;

public final class Controller {
  private static final Logger logger = LoggerFactory.getInstance(Controller.class);
  private static final Random rand = new Random();

  private static ControllerOptions options;

  public static String[] start(String[] argv) {
    logger.info("Starting OSCAR noising controller.");
    logger.info("Parsing arguments.");

    options = ControllerOptions.parse(argv);

    logger.info("Arguments parsed.");

    return options.InjectedArgs.split(" ");
  }

  public static void end() {
    options.ControllerOutput.terminate();

    logger.info("OSCAR noising controller routine ended.");
    System.exit(0);
  }

  /**
   * Signal the controller that a noise location has been reached
   *
   * @param instrumentedLocation instrumented location data
   */
  public static void signal(InstrumentedLocation instrumentedLocation) {
    long threadID = Thread.currentThread().getId();

    if (options.ControllerOutput != null)
      options.ControllerOutput.write(instrumentedLocation.getUUID() + " " + threadID);
  }

  /**
   * Make the injected program sleep
   *
   * @param instrumentedLocation instrumented location data
   */
  public static void sleep(InstrumentedLocation instrumentedLocation) {
    if (options.DisableNoise)
      return;

    if (!options.ActiveNoisePlacements.contains(instrumentedLocation.getNoisePlacement())) {
      logger.fine("Skipping noise placement type '" + instrumentedLocation.getNoisePlacement().name() + "'.");
      return;
    }

    // Get a random sleep length
    long sleepLength = options.MinSleepLength;
    sleepLength += Math.abs(rand.nextLong() % (1 + options.MaxSleepLength - options.MinSleepLength));

    // Sleep for a determined amount of time
    try {
      logger.fine("Sleeping for " + sleepLength + " ms.");
      Thread.yield(); // TODO should i add this here?
      Thread.sleep(sleepLength);
    } catch (InterruptedException e) {
      throw new RuntimeException("OSCAR sleep statement was interrupted.", e);
    }
  }

  // Capture exit codes
  public static void exit(int code) {
    if (code == 0) {
      logger.info("Detected exit code 0. Exiting gracefully.");
      end();
      System.exit(0);
    } else {
      logger.info("Detected exit code " + code + ". Exiting gracefully.");
      end();
      System.exit(code);
    }
  }
}
