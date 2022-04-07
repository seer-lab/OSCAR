package oscar.controller;

import oscar.controller.noise.NoisePlacement;
import oscar.controller.util.ControllerOptions;
import oscar.controller.util.ControllerOutput;
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
    if (options.OutputLocation != null) {
      String filename = options.OutputLocation + "/oscar_output_" + System.currentTimeMillis() + ".txt";
      ControllerOutput.write(filename, options.NoiseLocations);
    }

    logger.info("OSCAR noising controller routine ended.");
    System.exit(0);
  }

  public static void sleep(long locationId, String noisePlacementTypeShorthand) {
    if (options.DisableNoise)
      return;

    NoisePlacement noisePlacementType = NoisePlacement.fromString(noisePlacementTypeShorthand);
    long sleepLength;

    if (!options.ActiveNoisePlacements.contains(noisePlacementType)) {
      logger.fine("Skipping noise placement type '" + noisePlacementType.name() + "'.");
      return;
    }
    // Get sleep length
    if (options.OutputLocation != null && options.NoiseLocations.containsKey(locationId)) {
      sleepLength = options.NoiseLocations.get(locationId).getLength();
    } else {
      sleepLength = options.MinSleepLength + Math.abs(rand.nextLong() % (1 + options.MaxSleepLength - options.MinSleepLength));
      // noiseLocations.put(locationId, new SleepNoise(sleepLength)); \\ TODO fix this
    }

    // Sleep for a determined amount of time
    try {
      logger.fine("Sleeping for " + sleepLength + " ms for location '" + locationId + "'.");
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
