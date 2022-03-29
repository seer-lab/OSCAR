package oscar.utils;

import oscar.engine.Engine;
import oscar.utils.logger.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
      throw new RuntimeException("Failed to find class '%s'".formatted(classFile));

    byte[] classBytes;

    logger.fine("Attempting to write class '%s' to file.".formatted(clazz.getName()));

    try {
      classBytes = Files.readAllBytes(Path.of(url.toURI()));
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Failed to read class '%s'".formatted(classFile));
    }

    try {
      Files.deleteIfExists(Path.of(directoryAppended));
    } catch (IOException e) {
      logger.fine("File '%s' seems to already exist.".formatted(filename));
    }

    try {
      Files.createDirectories(Path.of(directoryAppended));
    } catch (IOException e) {
      logger.fine("Directory '%s' seems to already exist.".formatted(directoryAppended));
    }

    try {
      Files.write(Path.of(filename), classBytes, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      logger.fine("File '%s' seems to already exist, proceeding to write to it.".formatted(filename));
    }

    try {
      Files.write(Path.of(filename), classBytes, StandardOpenOption.WRITE);
    } catch (IOException e) {
      throw new RuntimeException("Failed to find class '%s'".formatted(classFile));
    }
  }
}
