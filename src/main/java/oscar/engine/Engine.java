package oscar.engine;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import oscar.controller.Controller;
import oscar.transformers.JimpleSceneTransformer;
import oscar.transformers.JimpleTransformer;
import oscar.transformers.injectors.ControllerInjector;
import oscar.transformers.injectors.ExitCaptureInjector;
import oscar.transformers.noisers.lock.ReentrantLockNoiser;
import oscar.transformers.noisers.shared.SharedVariableNoiser;
import oscar.transformers.noisers.sync.SynchronizedBlockNoiser;
import oscar.transformers.noisers.sync.SynchronizedMethodCallNoiser;
import oscar.transformers.noisers.thread.ThreadCreationNoiser;
import oscar.utils.ClassWriter;
import oscar.utils.logger.LoggerFactory;
import oscar.utils.logger.LoggerFormatter;
import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Engine {
  public static List<String> BlacklistedClasses = Arrays.asList(
      "java.",
      "sun.",
      "jdk.",
      "javax.",
      "com.",
      "org.",
      "kotlin.",
      "android.",
      "io.",
      "okhttp3.",
      "dagger.",
      "soot.",
      "oscar.",
      "$"
  );

  private static final Logger logger = LoggerFactory.getInstance(Engine.class);

  private static final String OSCAR_TEMP_DIR = ".oscar_temp";
  private static final String OSCAR_TEMP_EXTRACT_DIR = OSCAR_TEMP_DIR + File.separator + "extract";
  private static final String OSCAR_TEMP_GENERATED_DIR = OSCAR_TEMP_DIR + File.separator + "generated";

  private static final Random random = new Random();

  // Inject additional classes, not included in controller package
  private static final List<Class<?>> injectedClasses = Arrays.asList(
      LoggerFormatter.class,
      LoggerFactory.class
  );

  private static FILE_TYPE targetFileType;

  private final String targetFile;
  private static String mainClass = null;
  private final String targetDirectory;
  private final String outputDirectory;

  private final HashMap<String, ArrayList<Transform>> transformers = new HashMap<>();

  public Engine(String targetFile, String mainClass, String outputDirectory) {
    Engine.mainClass = mainClass;
    this.targetFile = targetFile;
    this.targetDirectory = Paths.get(targetFile).getParent().toString();
    this.outputDirectory = outputDirectory;

    // Register all transformers
    List.of(
        new ThreadCreationNoiser(),
        new SharedVariableNoiser(),

        new SynchronizedBlockNoiser(),
        new SynchronizedMethodCallNoiser(),
        new ReentrantLockNoiser(),

        new ControllerInjector(),
        new ExitCaptureInjector()
    ).forEach(this::registerTransformer);
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

        // Extract jar contents to directory
        //noinspection resource
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

    // Check main exists after loading necessary classes
    Scene.v().loadNecessaryClasses();
    Chain<SootClass> sc = Scene.v().getClasses();

    if (sc.stream().noneMatch(c -> c.getName().equals(mainClass)))
      throw new RuntimeException("Failed to find provided main class.");

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
      Options.v().set_output_dir(OSCAR_TEMP_GENERATED_DIR);

      String[] splitTargetJarPath = targetFile.split("/");
      String outputJarName = splitTargetJarPath[splitTargetJarPath.length - 1];

      // Try to create output folder
      try {
        Files.createDirectory(Paths.get(outputDirectory));
      } catch (IOException e) {
        throw new RuntimeException("Failed to delete output folder. Check directory permissions.", e);
      }

      //noinspection resource
      ZipFile jar = new ZipFile(outputDirectory + File.separator + outputJarName);

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

      /* TODO does not seem necessary
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

    logger.info("OSCAR instrumentation complete.");
  }

  public static void startTransformer(Class<? extends JimpleTransformer> transformerClass, Body body) {
    logger.fine("Starting regular transformer (Thread " + Thread.currentThread().getId() + ") '" +
                    transformerClass.getSimpleName() + "' for method '" +
                    body.getMethod().getSignature() + "'.");
  }

  public static void endTransformer(Class<? extends JimpleTransformer> transformerClass, Body body) {
    logger.fine("Finished regular transformer (Thread " + Thread.currentThread().getId() + ") '" +
                    transformerClass.getSimpleName() + "' for method '" +
                    body.getMethod().getSignature() + "'.");
  }

  public static void startSceneTransformer(Class<? extends JimpleSceneTransformer> transformerClass) {
    logger.fine("Starting scene transformer (Thread " + Thread.currentThread().getId() + ") '" +
                    transformerClass.getSimpleName() + "'.");
  }

  public static void endSceneTransformer(Class<? extends JimpleSceneTransformer> transformerClass) {
    logger.fine("Finished scene transformer (Thread " + Thread.currentThread().getId() + ") '" +
                    transformerClass.getSimpleName() + "'.");
  }

  public static void startCallgraphRoutine(Class<? extends JimpleSceneTransformer> transformerClass, SootMethod method) {
    logger.fine("Starting callgraph routine (Thread " + Thread.currentThread().getId() + ") '" +
                    transformerClass.getSimpleName() + "' for method '" +
                    method.getSignature() + "'.");
  }

  public static void endCallgraphRoutine(Class<? extends JimpleSceneTransformer> transformerClass, SootMethod method) {
    logger.fine("Ending callgraph routine (Thread " + Thread.currentThread().getId() + ") '" +
                    transformerClass.getSimpleName() + "' for method '" +
                    method.getSignature() + "'.");
  }

  public void registerTransformer(Transformer transformer) {
    String phase = "";
    String subPhase = "";

    if (transformer instanceof JimpleTransformer)
      phase = ((JimpleTransformer) transformer).getPhase();

    if (transformer instanceof JimpleSceneTransformer)
      phase = ((JimpleSceneTransformer) transformer).getPhase();

    if (transformer instanceof JimpleTransformer)
      subPhase = ((JimpleTransformer) transformer).getSubPhase();

    if (transformer instanceof JimpleSceneTransformer)
      subPhase = ((JimpleSceneTransformer) transformer).getSubPhase();

    transformers.putIfAbsent(phase, new ArrayList<>());
    transformers.get(phase).add(new Transform(subPhase, transformer));
    logger.info("Registered transformer " + transformer.getClass().getSimpleName() +
                    " with subphase " + subPhase + " in phase " + phase);
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

  public static String generateRandomString(int size) {
    byte[] arr = new byte[size];
    random.nextBytes(arr);

    StringBuilder sb = new StringBuilder();
    for (byte b : arr) {
      sb.append(String.format("%02X", b));
    }

    return sb.toString();
  }

  public static String getMainClass() {
    return mainClass;
  }

  public static boolean isClassBlacklisted(SootMethod method) {
    String className = method.getDeclaringClass().getName();

    boolean ignored = Engine.BlacklistedClasses.stream().anyMatch(className::startsWith);

    if (ignored)
      logger.fine("Ignoring blacklisted class: " + className);

    return ignored;
  }
}
