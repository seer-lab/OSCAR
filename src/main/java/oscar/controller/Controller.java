package oscar.controller;

import oscar.controller.noise.NoisePlacement;
import oscar.controller.noise.NoiseCategory;
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
    if (options.ControllerOutput != null)
      options.ControllerOutput.terminate();

    logger.info("OSCAR noising controller routine ended.");
    System.exit(0);
  }

  /**
   * Signal the controller that a noise location has been reached
   *
   * @param category instrumented location placement category
   * @param uuid     instrumented location generated uuid
   */
  public static void signal(NoiseCategory category, String uuid) {
    long threadID = Thread.currentThread().getId();

    if (!options.NoiseCategories.contains(category)) {
      logger.fine("Skipping noise category '" + category.name() + "'.");
      return;
    }

    if (options.ControllerOutput != null)
      options.ControllerOutput.write(threadID + " " + uuid);

    if (!options.Quiet)
      logger.fine("[" + "SIGNAL" + "]" + "[" + category.name() + "]" + "[" + uuid + "]");
  }

  /**
   * Make the injected program sleep
   *
   * @param placement instrumented location type
   * @param uuid      instrumented location generated uuid
   */
  public static void noise(NoisePlacement placement, String uuid) {
    if (options.DisableNoise)
      return;

    if (!options.NoisePlacements.contains(placement)) {
      logger.fine("Skipping noise placement type '" + placement.name() + "'.");
      return;
    }

    // Get a random noise intensity
    long noiseIntensity = options.MinNoiseIntensity;
    noiseIntensity += Math.abs(rand.nextLong() % (1 + options.MaxNoiseIntensity - options.MinNoiseIntensity));

    // Sleep for a determined amount of time
    try {
      if (!options.YieldMode) {
        logger.finest("[" + "SLEEP" + "]" +
                          "[" + placement.getCategory().name() + "]" +
                          "[" + placement.name() + "]" +
                          "[" + uuid + "]: "
                          + noiseIntensity + " MS."
        );

        Thread.sleep(noiseIntensity);
      } else {
        logger.finest("[" + "Yield" + "]" +
                          "[" + placement.getCategory().name() + "]" +
                          "[" + placement.name() + "]" +
                          "[" + uuid + "]: "
                          + noiseIntensity + " times."
        );

        for (int i = 0; i < noiseIntensity; i++)
          Thread.yield();
      }
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

  // Capture exit codes
  public static void exception(Exception exception) {
    logger.info("Caught exception in instrumented program:");

    for (StackTraceElement trace : exception.getStackTrace())
      logger.info("\t" + trace.toString());

    logger.info("Exiting gracefully.");
    end();
    System.exit(1);
  }
}
