package oscar.controllers;

import oscar.controllers.noise.SleepNoise;
import oscar.utils.logger.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Logger;

public final class ControllerOutputFile {
  private static final Logger logger = LoggerFactory.getInstance(ControllerOutputFile.class);

  public static void write(String filename, Map<Long, SleepNoise> noiseLocations) {
    logger.info("Writing output to file '" + filename + "'.");

    File noiseLocationsFile = new File(filename);

    logger.fine("Creating directories for output file'" + filename + "'.");
    try {
      Files.createDirectories(noiseLocationsFile.toPath().getParent());
    } catch (IOException e) {
      logger.info("Directories for file '" + filename + "' seem to already exist.");
    }

    logger.fine("Creating file '" + filename + "'.");
    try {
      Files.createFile(noiseLocationsFile.toPath());
    } catch (IOException e) {
      logger.info("File '" + filename + "' seems to already exist.");
    }

    logger.fine("Opening file '" + filename + "'.");
    FileWriter fw;
    try {
      fw = new FileWriter(noiseLocationsFile);
    } catch (IOException e) {
      throw new RuntimeException("Failed to open file '" + filename + "'.", e);
    }

    logger.fine("Writing entries to file '" + filename + "'.");
    try {
      for (Entry<Long, SleepNoise> entry : noiseLocations.entrySet()) {
        long id = entry.getKey();
        SleepNoise sn = entry.getValue();

        fw.write(id + " " + sn.getLength());
        fw.write("\n");
      }

      fw.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to write to file '" + filename + "'.", e);
    }
    logger.info("Noising locations successfully written to file '" + filename + "'.");
  }

  public static HashMap<Long, SleepNoise> read(String filename) {
    logger.info("Reading output file '" + filename + "'.");

    File noiseLocationsFile = new File(filename);

    logger.fine("Opening file '" + filename + "'.");
    Scanner scanner;
    try {
      scanner = new Scanner(noiseLocationsFile);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Failed to open file '" + filename + "'.", e);
    }

    HashMap<Long, SleepNoise> noiseLocations = new HashMap<>();

    logger.fine("Writing entries to file '" + filename + "'.");

    while (scanner.hasNext()) {
      long id = scanner.nextLong();
      long length = scanner.nextLong();

      noiseLocations.put(id, new SleepNoise(length));
    }

    scanner.close();
    logger.info("Noising locations successfully read from file '" + filename + "'.");

    return noiseLocations;
  }
}
