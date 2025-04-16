package oscar.utils;

import org.apache.commons.io.FileUtils;
import oscar.utils.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
      throw new RuntimeException("Failed to read class '" + classFile + "'", e);
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
      throw new RuntimeException("Failed to find class '" + classFile + "'", e);
    }
  }

  public static void writeClassPackageToFile(Class<?> clazz, String directory) {
    String classFile = clazz.getSimpleName() + ".class";
    URL url = clazz.getResource(classFile);

    if (url == null)
      throw new RuntimeException("Failed to find class '" + classFile + "'.");

    String packagePathLocation;
    try {
        packagePathLocation = Paths.get(url.toURI())
                                   .getParent()
                                   .toString();
    } catch (URISyntaxException e) {
        throw new RuntimeException("Invalid URI from URL: " + url, e);
    }

    String rawPath = url.getPath()
        .replace("%20", " ")
        .replace('\\', '/');

    String basePath = rawPath.substring(0, rawPath.length() - classFile.length() - 1);

    String[] parts = basePath.split("/oscar/");

    if (parts.length < 2) {
        throw new IllegalStateException("Could not find '/oscar/' in path: " + basePath);
    }

    String packageOutputLocation = directory + File.separator + "oscar" + File.separator + parts[1];

    try {
      FileUtils.copyDirectory(new File(packagePathLocation), new File(packageOutputLocation));
    } catch (IOException e) {
      throw new RuntimeException("Failed to copy package '" + packagePathLocation + "'.", e);
    }
  }
}
