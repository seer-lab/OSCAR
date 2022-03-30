package oscar.utils;

import oscar.utils.logger.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

public final class ClassWriter {
  private static final Logger logger = LoggerFactory.getInstance(ClassWriter.class);

  public static void writeToFile(Class<?> clazz, String directory) {
    String classFile = clazz.getSimpleName() + ".class";
    URL url = clazz.getResource(classFile);
    String directoryAppended = directory + "/" + clazz.getPackageName().replace(".", "/");
    String filename = directoryAppended + "/" + classFile;

    if (url == null)
      throw new RuntimeException("Failed to find class '" + classFile + "'.");

    byte[] classBytes;

    logger.fine("Attempting to write class '" + clazz.getName() + "' to file.");

    try {
      classBytes = Files.readAllBytes(Paths.get(url.toURI()));
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Failed to read class '" + classFile + "'",e);
    }

    try {
      Files.deleteIfExists(Paths.get(directoryAppended));
    } catch (IOException e) {
      logger.fine("File '" + filename + "' seems to already exist.");
    }

    try {
      Files.createDirectories(Paths.get(directoryAppended));
    } catch (IOException e) {
      logger.fine("Directory '" + directoryAppended + "' seems to already exist.");
    }

    try {
      Files.write(Paths.get(filename), classBytes, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      logger.fine("File '" + filename + "' seems to already exist, proceeding to write to it.");
    }

    try {
      Files.write(Paths.get(filename), classBytes, StandardOpenOption.WRITE);
    } catch (IOException e) {
      throw new RuntimeException("Failed to find class '" + classFile + "'",e);
    }
  }
}
