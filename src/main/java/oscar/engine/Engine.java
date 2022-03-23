package oscar.engine;

import oscar.utils.ConfigParser;
import oscar.utils.logger.LoggerFactory;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Engine {
  private static final Logger logger = LoggerFactory.getInstance(Engine.class);

  private static final Map<String, List<String>> tags = new HashMap<>();

  public static void initialize() {
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
    }

    if (targetFileType == FILE_TYPE.CLASS) {
      Options.v().set_soot_classpath(ConfigParser.TargetDirectory);
      SootClass sc = Scene.v().loadClassAndSupport(ConfigParser.MainClass);
      sc.setApplicationClass();
    }

    Scene.v().loadNecessaryClasses();

    logger.info("Soot engine initialization complete.");

    // Options.v().set_src_prec(Options.src_prec_java);
    // Options.v().set_android_jars(androidJar);
    //Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
    //Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
    //Options.v().set_process_multiple_dex(true);
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

  private static void copyManifest(String sourceJar, String targetJar) {
      Path zipFilePath = Paths.get(sourceJar);

      try {
        FileSystem zip = FileSystems.newFileSystem(zipFilePath);
      } catch (IOException e) {
        throw new RuntimeException("Failed to read source JAR file.");
      }

      try () {
        Path manifestFile = zipFileSystem.getPath("META-INF/MANIFEST.MF");
        String newManifestContent;
        // Read from MANIFEST.MF.
        try (Stream<String> lines = Files.lines(manifestFile, StandardCharsets.UTF_8)) {
          newManifestContent = lines.filter(l -> !l.startsWith("Class-Path entry I want to remove"))
                                    .collect(Collectors.joining("\n"));
        }
        // Replace MANIFEST.MF content.
        Files.write(manifestFile, newManifestContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    }
  }
}
