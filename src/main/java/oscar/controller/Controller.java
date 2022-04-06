package oscar.controller;

import oscar.controller.noise.NoisePlacement;
import oscar.controller.noise.SleepNoise;
import oscar.controller.util.ControllerOptions;
import oscar.controller.util.ControllerConfigFile;
import oscar.controller.util.ControllerOutput;
import oscar.utils.logger.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Controller {
  private static final Logger logger = LoggerFactory.getInstance(Controller.class);
  private static final Random rand = new Random();

  private static final ConcurrentHashMap<Long, SleepNoise> noiseLocations = new ConcurrentHashMap<>(); // TODO Should this be a treemap?
  private static final HashSet<NoisePlacement> activeNoisePlacements = new HashSet<>(Arrays.asList(NoisePlacement.values()));

  private static ControllerOptions options;

  public static String[] start(String[] argv) {
    logger.info("Starting OSCAR noising controller.");
    logger.info("Parsing arguments.");

    options = ControllerOptions.parse(argv);

    logger.info("Arguments parsed.");

    // Read the configuration file if it exists
    if (options.ConfigFile != null)
      readConfigFile();

    return options.InjectedArgs.split(" ");
  }

  private static void captureShutdown() {
    logger.info("Unexpected ending captured.");
  }

  public static void end() {
    if (options.OutputLocation != null) {
      String filename = options.OutputLocation + "/oscar_output_" + System.currentTimeMillis() + ".txt";
      ControllerOutput.write(filename, noiseLocations);
    }

    logger.info("OSCAR noising controller routine ended.");
    System.exit(0);
  }

  public static void sleep(long locationId, String noisePlacementTypeShorthand) {
    long sleepLength = 0;
    NoisePlacement noisePlacementType = NoisePlacement.fromString(noisePlacementTypeShorthand);

    if (!activeNoisePlacements.contains(noisePlacementType)) {
      logger.fine("Skipping noise placement type '" + noisePlacementType.name() + "'.");
      return;
    }

    // Get sleep length
    if (options.OutputLocation != null && noiseLocations.containsKey(locationId)) {
      sleepLength = noiseLocations.get(locationId).getLength();
    } else {
      sleepLength = options.MinSleepLength + Math.abs(rand.nextLong() % (options.MaxSleepLength - options.MinSleepLength));
      // noiseLocations.put(locationId, new SleepNoise(sleepLength)); \\ TODO fix this
    }

    // Sleep for a determined amount of time
    try {
      logger.fine("Sleeping for " + sleepLength + " ms for location '" + locationId + "'");
      // Thread.yield(); TODO should i add this here?
      Thread.sleep(sleepLength);
    } catch (InterruptedException e) {
      throw new RuntimeException("OSCAR sleep statement was interrupted.", e);
    }
  }

  public static void readConfigFile() {
    logger.info("Looking for config file in location '" + options.ConfigFile + "'.");

    Properties props = ControllerConfigFile.getConfigFile(options.ConfigFile);

    logger.info("Found client.properties file. Reading properties.");

    // Read basic properties
    options.MaxSleepLength = ControllerConfigFile.parseLong(props, "max_sleep_length", "400");
    options.MinSleepLength = ControllerConfigFile.parseLong(props, "min_sleep_length", "0");

    // Read noise location file, if provided
    String noiseLocationsFile = props.getProperty("noise_locations_file");
    if (noiseLocationsFile != null) {
      HashMap<Long, SleepNoise> readNoiseLocations = ControllerOutput.read(noiseLocationsFile);
      noiseLocations.putAll(readNoiseLocations);
    }

    // Read which noise placements
    String noisePlacements = props.getProperty("noise_placements");
    if (noisePlacements != null) {
      activeNoisePlacements.clear();
      activeNoisePlacements.addAll(Arrays.stream(noisePlacements.split(","))
                                         .map(NoisePlacement::fromString)
                                         .collect(Collectors.toSet()));
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
