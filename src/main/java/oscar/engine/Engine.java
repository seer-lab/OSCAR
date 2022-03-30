package oscar.engine;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import oscar.controllers.OscarController;
import oscar.utils.ClassWriter;
import oscar.utils.ConfigParser;
import oscar.utils.logger.LoggerFactory;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Engine {
  private static final Logger logger = LoggerFactory.getInstance(Engine.class);

  private static final String OSCAR_TEMP_DIR = ".oscar_temp";
  private static final String OSCAR_EXTRACT_DIR = OSCAR_TEMP_DIR + "/extract";
  private static final String OSCAR_GENERATED_DIR = OSCAR_TEMP_DIR + "/generated";

  public static final HashSet<Class<?>> INJECTED_OSCAR_CLASSES = new HashSet<>();

  static {
    INJECTED_OSCAR_CLASSES.add(OscarController.class);
    INJECTED_OSCAR_CLASSES.add(LoggerFactory.class);
  }

  private static FILE_TYPE targetFileType;
  private static long currentLocationID = 0L;

  public static void start() {
    // Get target file type and check if valid
    targetFileType = getInputFileType(ConfigParser.TargetFile);

    if (targetFileType == FILE_TYPE.INVALID)
      throw new RuntimeException("Invalid input file type.");

    // Set Soot configurations
    G.reset();

    logger.info("Initializing Soot engine...");

    Options.v().set_allow_phantom_refs(true);
    Options.v().set_whole_program(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_validate(true);
    Options.v().set_include_all(true);
    Options.v().set_output_format(Options.output_format_class);
    Options.v().set_output_dir(ConfigParser.OutputDirectory);
    Options.v().set_soot_classpath(OSCAR_EXTRACT_DIR);
    Options.v().set_process_dir(List.of(OSCAR_EXTRACT_DIR));
    Options.v().set_force_overwrite(true);

    // Try to create temp folder
    try {
      FileUtils.deleteDirectory(new File(OSCAR_TEMP_DIR));
      Files.createDirectory(Path.of(OSCAR_TEMP_DIR));
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete temp folder. Check directory permissions.", e);
    }

    // Check if JAR file and process accordingly
    switch (targetFileType) {
      case JAR -> {
        Options.v().set_output_dir(OSCAR_GENERATED_DIR);

        // Delete target jar if exists
        try {
          Files.deleteIfExists(Path.of(ConfigParser.OutputDirectory + "/out.jar"));
        } catch (IOException e) {
          throw new RuntimeException("Failed to delete previously generated file. Check file permissions.", e);
        }

        // Extract jar contents to directory
        ZipFile jar = new ZipFile(ConfigParser.TargetFile);

        try {
          jar.extractAll(OSCAR_EXTRACT_DIR);
        } catch (IOException e) {
          throw new RuntimeException("Failed to extract jar. Check permissions.", e);
        }
      }

      case CLASS -> {
        try {
          FileUtils.copyDirectory(new File(ConfigParser.TargetDirectory), new File(OSCAR_EXTRACT_DIR));
        } catch (IOException e) {
          throw new RuntimeException("Failed to copy target files to temporary directory.");
        }
      }
    }

    INJECTED_OSCAR_CLASSES.forEach(c -> ClassWriter.writeToFile(c, OSCAR_EXTRACT_DIR));

    SootClass sc = Scene.v().loadClassAndSupport(ConfigParser.MainClass);
    sc.setApplicationClass();

    Scene.v().loadNecessaryClasses();

    logger.info("Soot engine initialization complete.");
  }

  public static void end() {
    switch (targetFileType) {
      case JAR -> {
        ZipFile jar = new ZipFile(ConfigParser.OutputDirectory + "/out.jar");
        try {
          for (File tempFile : getDirectoryContent(OSCAR_EXTRACT_DIR))
            if (tempFile.isDirectory())
              jar.addFolder(tempFile);
            else
              jar.addFile(tempFile);

          for (File tempFile : getDirectoryContent(OSCAR_GENERATED_DIR))
            if (tempFile.isDirectory())
              jar.addFolder(tempFile);
            else
              jar.addFile(tempFile);
        } catch (ZipException e) {
          throw new RuntimeException("Failed to add files from temp folder to zip.", e);
        }

        // Copy generated files over
        try {
          File srcDir = new File(OSCAR_GENERATED_DIR);
          File destDir = new File(ConfigParser.TargetDirectory);
          FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
          throw new RuntimeException("Failed to copy generated sources to output folder. Check directory permissions.", e);
        }

        // Delete temp folder
        try {
          FileUtils.deleteDirectory(new File(OSCAR_TEMP_DIR));
        } catch (IOException e) {
          throw new RuntimeException("Failed to delete temp folder. Check directory permissions.", e);
        }
      }
    }
  }

  private static FILE_TYPE getInputFileType(String file) {
    String[] tokenizedFilePath = file.split("\\.");

    return switch (tokenizedFilePath[tokenizedFilePath.length - 1]) {
      case "jar" -> FILE_TYPE.JAR;
      case "class" -> FILE_TYPE.CLASS;
      default -> FILE_TYPE.INVALID;
    };
  }

  private enum FILE_TYPE {
    JAR,
    CLASS,
    INVALID
  }

  private static List<File> getDirectoryContent(String dir) {
    File dirFile = new File(dir);
    int maxDirDepth = dirFile.getPath().split("/").length + 1;

    return FileUtils.listFilesAndDirs(
                        dirFile,
                        FileFilterUtils.trueFileFilter(),
                        FileFilterUtils.trueFileFilter()
                    )
                    .stream()
                    .filter(f -> f.getPath().split("/").length == maxDirDepth)
                    .collect(Collectors.toCollection(ArrayList::new));
  }

  private synchronized static long generateLocationID() {
    return currentLocationID++;
  }
}
