package oscar.engine;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import oscar.controller.Controller;
import oscar.transformers.CustomJimpleTransformer;
import oscar.utils.ClassWriter;
import oscar.utils.logger.LoggerFactory;
import oscar.utils.logger.LoggerFormatter;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Engine {
  private static final Logger logger = LoggerFactory.getInstance(Engine.class);

  private static final String OSCAR_TEMP_DIR = ".oscar_temp";
  private static final String OSCAR_TEMP_EXTRACT_DIR = OSCAR_TEMP_DIR + File.separator + "extract";
  private static final String OSCAR_TEMP_GENERATED_DIR = OSCAR_TEMP_DIR + File.separator + "generated";

  // Inject additional classes, not included in controller package
  private static final List<Class<?>> injectedClasses = Arrays.asList(
      LoggerFormatter.class,
      LoggerFactory.class
  );

  private static FILE_TYPE targetFileType;
  private static long currentLocationID = 0L;

  private final String targetFile;
  private final String mainClass;
  private final String targetDirectory;
  private final String outputDirectory;

  private final HashMap<String, ArrayList<Transform>> transformers = new HashMap<>();

  public Engine(String targetFile, String mainClass, String outputDirectory) {
    this.targetFile = targetFile;
    this.mainClass = mainClass;
    this.targetDirectory = Paths.get(targetFile).getParent().toString();
    this.outputDirectory = outputDirectory;
  }

  public void run() {
    // Get target file type and check if valid
    targetFileType = getInputFileType(targetFile);

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
    Options.v().set_output_dir(outputDirectory);
    Options.v().set_soot_classpath(OSCAR_TEMP_EXTRACT_DIR);
    Options.v().set_process_dir(Collections.singletonList(OSCAR_TEMP_EXTRACT_DIR));
    Options.v().set_force_overwrite(true);
    Options.v().set_num_threads(1); // This will hopefully enforce an order

    // Try to create temp folder
    try {
      FileUtils.deleteDirectory(new File(OSCAR_TEMP_DIR));
      Files.createDirectory(Paths.get(OSCAR_TEMP_DIR));
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete temp folder. Check directory permissions.", e);
    }

    // Delete output folder if exists
    try {
      FileUtils.deleteDirectory(new File(outputDirectory));
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete output directory. Check file permissions.", e);
    }

    // Check if JAR file and process accordingly
    switch (targetFileType) {
      case JAR:
        Options.v().set_output_dir(OSCAR_TEMP_GENERATED_DIR);

        // Delete target jar if exists
        try {
          Files.deleteIfExists(Paths.get(outputDirectory + File.separator + "out.jar"));
        } catch (IOException e) {
          throw new RuntimeException("Failed to delete previously generated file. Check file permissions.", e);
        }

        // Extract jar contents to directory
        ZipFile jar = new ZipFile(targetFile);

        try {
          jar.extractAll(OSCAR_TEMP_EXTRACT_DIR);
        } catch (IOException e) {
          throw new RuntimeException("Failed to extract jar. Check permissions.", e);
        }
        break;

      case CLASS:
        try {
          FileUtils.copyDirectory(new File(targetDirectory), new File(OSCAR_TEMP_EXTRACT_DIR));
        } catch (IOException e) {
          throw new RuntimeException("Failed to copy target files to temporary directory.", e);
        }
        break;
    }

    ClassWriter.writeClassPackageToFile(Controller.class, OSCAR_TEMP_EXTRACT_DIR);
    injectedClasses.forEach(c -> ClassWriter.writeToFile(c, OSCAR_TEMP_EXTRACT_DIR));

    try {
      SootClass sc = Scene.v().loadClassAndSupport(mainClass);
      sc.setApplicationClass();
    } catch (NullPointerException e) {
      throw new RuntimeException("Failed to load main class. Check path.", e);
    }

    Scene.v().loadNecessaryClasses();

    logger.info("Soot engine initialization complete.");

    logger.info("Registering Soot packs.");
    transformers.forEach((p, t) -> t.forEach(PackManager.v().getPack(p)::add));
    logger.info("Soot packs registered.");

    // Run Soot packs
    logger.info("Running Soot packs.");
    PackManager.v().runPacks();
    logger.info("Soot packs finished running.");

    end();
  }

  public void end() {
    // Write the result of packs in outputPath
    logger.info("Writing Soot output.");
    PackManager.v().writeOutput();

    // If output is jar, create jar
    if (targetFileType == FILE_TYPE.JAR) {
      // Try to create output folder
      try {
        Files.createDirectory(Paths.get(outputDirectory));
      } catch (IOException e) {
        throw new RuntimeException("Failed to delete output folder. Check directory permissions.", e);
      }

      ZipFile jar = new ZipFile(outputDirectory + File.separator + "oscar_out.jar");

      try {
        for (File tempFile : getDirectoryContent(OSCAR_TEMP_EXTRACT_DIR))
          if (tempFile.isDirectory())
            jar.addFolder(tempFile);
          else
            jar.addFile(tempFile);

        for (File tempFile : getDirectoryContent(OSCAR_TEMP_GENERATED_DIR))
          if (tempFile.isDirectory())
            jar.addFolder(tempFile);
          else
            jar.addFile(tempFile);
      } catch (ZipException e) {
        throw new RuntimeException("Failed to add files from temp folder to zip.", e);
      }

      /*
      // Copy generated files over
      try {
        File srcDir = new File(OSCAR_TEMP_GENERATED_DIR);
        File destDir = new File(targetDirectory);
        FileUtils.copyDirectory(srcDir, destDir);
      } catch (IOException e) {
        throw new RuntimeException("Failed to copy generated sources to output folder. Check directory permissions.", e);
      }
       */

      // Delete temp folder
      try {
        FileUtils.deleteDirectory(new File(OSCAR_TEMP_DIR));
      } catch (IOException e) {
        throw new RuntimeException("Failed to delete temp folder. Check directory permissions.", e);
      }
    }
  }

  public void registerTransformer(CustomJimpleTransformer transformer) {
    transformers.putIfAbsent(transformer.getPhase(), new ArrayList<>());
    transformers.get(transformer.getPhase()).add(new Transform(transformer.getSubPhase(), transformer));
    logger.info("Registered transformer " + transformer.getClass().getSimpleName()
                    + " with subphase " + transformer.getSubPhase()
                    + " in phase " + transformer.getPhase());
  }

  private static FILE_TYPE getInputFileType(String file) {
    String[] tokenizedFilePath = file.split("\\.");

    FILE_TYPE type;

    switch (tokenizedFilePath[tokenizedFilePath.length - 1]) {
      case "jar":
        type = FILE_TYPE.JAR;
        break;
      case "class":
        type = FILE_TYPE.CLASS;
        break;
      default:
        type = FILE_TYPE.INVALID;
        break;
    }

    return type;
  }

  private enum FILE_TYPE {
    JAR,
    CLASS,
    INVALID
  }

  private static List<File> getDirectoryContent(String dir) {
    File dirFile = new File(dir);
    int maxDirDepth = dirFile.getPath().split(File.separator).length + 1;

    return FileUtils.listFilesAndDirs(
                        dirFile,
                        FileFilterUtils.trueFileFilter(),
                        FileFilterUtils.trueFileFilter()
                    )
                    .stream()
                    .filter(f -> f.getPath().split(File.separator).length == maxDirDepth)
                    .collect(Collectors.toCollection(ArrayList::new));
  }

  public synchronized static long generateLocationID() {
    return currentLocationID++;
  }
}
