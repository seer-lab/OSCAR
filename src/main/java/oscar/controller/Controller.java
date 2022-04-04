package oscar.controller;

import oscar.controller.noise.NoisePlacement;
import oscar.controller.noise.SleepNoise;
import oscar.controller.util.ControllerArgs;
import oscar.controller.util.ControllerConfigFile;
import oscar.controller.util.ControllerOutput;
import oscar.utils.logger.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Controller {
  private static final Logger logger = LoggerFactory.getInstance(Controller.class);
  private static final Random rand = new Random();

  private static final ConcurrentHashMap<Long, SleepNoise> noiseLocations = new ConcurrentHashMap<>(); // TODO Should this be a treemap?
  private static final HashSet<NoisePlacement> activeNoisePlacements = new HashSet<>(Arrays.asList(NoisePlacement.values()));

  public static ControllerArgs args;

  private static String start(String[] argv) {
    logger.info("Starting OSCAR noising controller.");
    logger.info("Parsing arguments.");

    args = ControllerArgs.parse(argv);

    // Set Logger level
    if (args.Verbose)
      LoggerFactory.setLevel(Level.ALL);

    if (args.Quiet)
      LoggerFactory.setLevel(Level.OFF);


    logger.info("Arguments parsed.");

    if (args.ConfigFile != null)
      readConfigFile();

    return args.InjectedArgs;
  }

  public static void end() {
    if (args.OutputLocation != null) {
      String filename = args.OutputLocation + "/oscar_output_" + System.currentTimeMillis() + ".txt";
      ControllerOutput.write(filename, noiseLocations);
    }
  }

  public static void sleep(long locationId, String noisePlacementTypeShorthand) {
    long sleepLength = 0;
    NoisePlacement noisePlacementType = NoisePlacement.fromString(noisePlacementTypeShorthand);

    if (!activeNoisePlacements.contains(noisePlacementType)) {
      logger.fine("Skipping noise placement type '" + noisePlacementType.name() + "'.");
      return;
    }

    // Get sleep length
    if (noiseLocations.containsKey(locationId)) {
      sleepLength = noiseLocations.get(locationId).getLength();
    } else {
      sleepLength = args.MinSleepLength + Math.abs(rand.nextLong() % args.MaxSleepLength);
      noiseLocations.put(locationId, new SleepNoise(sleepLength));
    }

    // Sleep for a determined amount of time
    if (sleepLength != 0) {
      try {
        logger.fine("Sleeping for " + sleepLength + " ms for location '" + locationId + "'");
        Thread.yield();
        Thread.sleep(sleepLength);
      } catch (InterruptedException e) {
        throw new RuntimeException("OSCAR sleep statement was interrupted.", e);
      }
    } else
      logger.fine("Sleeping disabled or 0 ms for location with id '" + locationId + "'.");
  }

  public static void readConfigFile() {
    logger.info("Looking for config file in location '" + args.ConfigFile + "'.");

    Properties props = ControllerConfigFile.getConfigFile(args.ConfigFile);

    logger.info("Found client.properties file. Reading properties.");

    // Read basic properties
    args.MaxSleepLength = ControllerConfigFile.parseLong(props, "max_sleep_length", "400");
    args.MinSleepLength = ControllerConfigFile.parseLong(props, "min_sleep_length", "0");

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
}
