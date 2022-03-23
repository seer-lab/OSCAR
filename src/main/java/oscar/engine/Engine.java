package oscar.engine;

import oscar.utils.ConfigParser;
import oscar.utils.logger.LoggerFactory;
import soot.*;
import soot.options.Options;

import java.util.*;
import java.util.logging.Logger;

public class Engine {
  private static final Logger logger = LoggerFactory.getInstance(Engine.class);

  private static final Map<String, List<String>> tags = new HashMap<>();

  public static void start() {
    // Get target file type and check if valid
    FILE_TYPE targetFileType = getInputFileType(ConfigParser.TargetFile);

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
    Options.v().set_force_overwrite(true);

    // Check if JAR or class file and process accordingly
    if (targetFileType == FILE_TYPE.JAR) {
      Options.v().set_output_jar(true);
      Options.v().set_process_dir(List.of(ConfigParser.TargetFile));
      //Options.v().set_process_jar_dir(List.of(ConfigParser.TargetFile));
    }

    if (targetFileType == FILE_TYPE.CLASS) {
      Options.v().set_soot_classpath(ConfigParser.TargetDirectory);
      SootClass sc = Scene.v().loadClassAndSupport(ConfigParser.MainClass);
      sc.setApplicationClass();
    }

    Scene.v().loadNecessaryClasses();

    logger.info("Soot engine initialization complete.");
  }

  public static void end() {
  }

  private static FILE_TYPE getInputFileType(String file) {
    String[] tokenizedFilePath = file.split("\\.");

    return switch (tokenizedFilePath[tokenizedFilePath.length - 1]) {
      case "jar" -> FILE_TYPE.JAR;
      case "class" -> FILE_TYPE.CLASS;
      default -> FILE_TYPE.INVALID;
    };
  }

  public static void registerTagger(String name) {
    if (tags.containsKey(name))
      throw new RuntimeException("Tagger '%s' already exists.".formatted(name));

    tags.put(name, new ArrayList<>());
  }

  public static void tag(String tagger, String tag) {
    if (!tags.containsKey(tagger))
      throw new RuntimeException("Tagger '%s' not found.".formatted(tagger));

    tags.get(tagger).add(tag);
  }

  private enum FILE_TYPE {
    JAR,
    CLASS,
    INVALID
  }
/*
  private static void copyManifest(String sourceJar, String targetJar) {
    FileSystem targetZip;
    FileSystem sourceZip;

    try {
      sourceZip = FileSystems.newFileSystem(sourceJar);
      targetZip = FileSystems.newFileSystem(targetJar);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read source JAR file.");
    }

    Path sourceManifest = sourceZip.getPath("META-INF/MANIFEST.MF");
    Path targetManifest = targetZip.getPath("META-INF/MANIFEST.MF");

    try {
      Files.copy(sourceManifest, targetManifest, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write manifest to JAR file.");
    }
  }*/
}
