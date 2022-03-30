package oscar.controllers;

import org.apache.commons.io.FileUtils;
import oscar.utils.logger.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

public final class OscarController {
  private static final Logger logger = LoggerFactory.getInstance(OscarController.class);
  private final static String OSCAR_FOLDER = ".oscar";
  private final static String OSCAR_PROPERTIES = OSCAR_FOLDER + "/" + "config.properties";
  private final static String OSCAR_OUTPUT_CONFIG = OSCAR_FOLDER + "/" + "noised_locations";

  private static final Random rand = new Random();

  private static final Map<Long, SleepNoise> noiseLocations = Collections.synchronizedMap(new HashMap<>()); // TODO Should this be a treemap?
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
      logger.info("Writing locations to file '" + OSCAR_PROPERTIES + "'.");

      String filename = OSCAR_OUTPUT_CONFIG + "_" + System.currentTimeMillis() + ".txt";
      File noiseLocationsFile = new File(filename);

      logger.fine("Creating directories '" + OSCAR_PROPERTIES + "'.");
      try {
        Files.createDirectories(noiseLocationsFile.toPath().getParent());
      } catch (IOException e) {
        logger.info("Directories for file '" + OSCAR_PROPERTIES + "' seem to already exist.");
      }

      logger.fine("Creating file '" + OSCAR_PROPERTIES + "'.");
      try {
        Files.createFile(noiseLocationsFile.toPath());
      } catch (IOException e) {
        logger.info("File '" + OSCAR_PROPERTIES + "' seems to already exist.");
      }

      logger.fine("Opening file '" + OSCAR_PROPERTIES + "'.");
      FileWriter fw;
      try {
        fw = new FileWriter(noiseLocationsFile);
      } catch (IOException e) {
        throw new RuntimeException("Failed to open file '" + filename + "'.", e);
      }

      logger.fine("Writing entries to file '" + OSCAR_PROPERTIES + "'.");
      try {
        for (Map.Entry<Long, SleepNoise> entry : noiseLocations.entrySet()) {
          long id = entry.getKey();
          SleepNoise sn = entry.getValue();

          fw.write(id + " " + sn.getLength());
          fw.write("\n");
        }

        fw.close();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write to file '" + filename + "'.", e);
      }
      logger.info("Noising locations successfully written to file '" + OSCAR_PROPERTIES + "'.");
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
    try {
      maxSleepLength = Long.parseLong(props.getProperty("max_sleep_length", "40"));
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid property value for 'max_sleep_length'.", e);
    }

    // TODO loadednoiselocations
  }
}
