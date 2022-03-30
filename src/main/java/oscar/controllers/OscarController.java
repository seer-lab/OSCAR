package oscar.controllers;

import oscar.utils.logger.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class OscarController {
  private static final Logger logger = LoggerFactory.getInstance(OscarController.class);
  private final static String OSCAR_FOLDER = ".oscar";
  private final static String OSCAR_PROPERTIES = OSCAR_FOLDER + "/" + "config.properties";
  private final static String OSCAR_OUTPUT_CONFIG = OSCAR_FOLDER + "/" + "noised_locations";

  private static final Random rand = new Random();

  private static final ConcurrentHashMap<Long, SleepNoise> noiseLocations = new ConcurrentHashMap<>(); // TODO Should this be a treemap?
  private static boolean loadedNoiseLocations = false;
  private static long maxSleepLength = 40L;
  private static Properties props = null;

  public static void start() {
    logger.info("Starting OSCAR noising controller.");

    logger.info("Looking for config file in location '" + OSCAR_PROPERTIES + "'.");

    try {
      props = new Properties();
      props.load(new FileInputStream(OSCAR_PROPERTIES));
      readConfigFile();
    } catch (IOException e) {
      logger.info("Failed to find client.properties file in '" + OSCAR_PROPERTIES + "'.");
    }
  }

  public static void end() {
    if (!loadedNoiseLocations) {
      String filename = OSCAR_OUTPUT_CONFIG + "_" + System.currentTimeMillis() + ".txt";
      ControllerOutputFile.write(filename, noiseLocations);
    }
  }

  public static void sleep(long locationId) {
    long sleepLength = 0;

    // Get sleep length
    if (noiseLocations.containsKey(locationId)) {
      sleepLength = noiseLocations.get(locationId).getLength();
    } else {
      if (rand.nextBoolean()) {
        sleepLength = Math.abs(rand.nextLong() % maxSleepLength);
        noiseLocations.put(locationId, new SleepNoise(sleepLength));
      } else
        noiseLocations.put(locationId, new SleepNoise(0L));
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
    logger.info("Found client.properties file. Reading properties.");

    // Read basic properties
    try {
      maxSleepLength = Long.parseLong(props.getProperty("max_sleep_length", "40"));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid property value for 'max_sleep_length'.", e);
    }

    // Read noise location file, if provided
    String noiseLocationsFile = props.getProperty("noise_locations_file");
    if (noiseLocationsFile != null) {
      HashMap<Long, SleepNoise> readNoiseLocations = ControllerOutputFile.read(noiseLocationsFile);
      noiseLocations.putAll(readNoiseLocations);
      loadedNoiseLocations = true;
    }
  }
}
