package oscar.controllers;

import oscar.utils.logger.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public final class OscarController {
  private static final Logger logger = LoggerFactory.getInstance(OscarController.class);
  private final static String OSCAR_FOLDER = ".oscar";
  private final static String OSCAR_PROPERTIES = OSCAR_FOLDER + "/" + "config.properties";
  private final static String OSCAR_OUTPUT_CONFIG = OSCAR_FOLDER + "/" + "noised_locations";

  private static final Random rand = new Random();

  // Noise types -------------------------------------------------
  private record SleepNoise(long length) {}
  // -------------------------------------------------------------

  private static final Map<Long, SleepNoise> noiseLocations = Collections.synchronizedMap(new HashMap<>()); // TODO Should this be a treemap?
  private static boolean loadedNoiseLocations = false;
  private static long maxSleepLength = 4000L;
  private static Properties props = null;

  public static void start() {
    logger.info("Starting OSCAR noising controller.");

    logger.info("Looking for config file in location '%s'.".formatted(OSCAR_PROPERTIES));

    try {
      props = new Properties();
      props.load(new FileInputStream(OSCAR_PROPERTIES));
    } catch (IOException e) {
      logger.info("Failed to find client.properties file in '%s'.".formatted(OSCAR_PROPERTIES));
    }

    // Load properties from file
    if (props != null)
      readConfigFile();
  }

  public static void end() {
    if (!loadedNoiseLocations) {
      logger.info("Writing locations to file '%s'.".formatted(OSCAR_PROPERTIES));

      String filename = OSCAR_OUTPUT_CONFIG + "_" + System.currentTimeMillis() + ".txt";
      File noiseLocationsFile = new File(URI.create(filename));

      logger.fine("Creating file '%s'.".formatted(OSCAR_PROPERTIES));
      try {
        noiseLocationsFile.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException("Failed to create file %s'.".formatted(filename));
      }

      logger.fine("Opening file '%s'.".formatted(OSCAR_PROPERTIES));
      FileWriter fw;
      try {
        fw = new FileWriter(noiseLocationsFile);
      } catch (IOException e) {
        throw new RuntimeException("Failed to open file '%s'.".formatted(filename));
      }

      logger.fine("Writing entries to file '%s'.".formatted(OSCAR_PROPERTIES));
      try {
        for (Map.Entry<Long, SleepNoise> entry : noiseLocations.entrySet()) {
          long id = entry.getKey();
          SleepNoise sn = entry.getValue();

          fw.write(id + " " + sn.length);
          fw.write("\n");
        }

        fw.close();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write to file '%s'.".formatted(filename));
      }
      logger.info("Noising locations successfully written to file '%s'.".formatted(OSCAR_PROPERTIES));
    }
  }

  public static void sleep(long locationId) {
    long sleepLength = 0;

    // Get sleep length
    if (loadedNoiseLocations) {
      sleepLength = noiseLocations.containsKey(locationId) ? noiseLocations.get(locationId).length : 0;
    } else {
      if (rand.nextBoolean()) {
        sleepLength = rand.nextLong(maxSleepLength);
        noiseLocations.put(locationId, new SleepNoise(sleepLength));
      } else
        noiseLocations.put(locationId, null);
    }

    // Sleep for a determined amount of time
    if (sleepLength != 0) {
      try {
        logger.fine("Sleeping for %d ms for location %d".formatted(sleepLength, locationId));
        Thread.yield();
        Thread.sleep(sleepLength);
      } catch (InterruptedException e) {
        throw new RuntimeException("OSCAR sleep statement was interrupted.");
      }
    } else
      logger.fine("Sleeping disabled or 0 ms for location %d".formatted(locationId));
  }

  public static void readConfigFile() {
    try {
      maxSleepLength = Long.parseLong(props.getProperty("max_sleep_length", "4000"));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid property value for 'max_sleep_length'.", e);
    }

    // TODO loadednoiselocations
  }
}
