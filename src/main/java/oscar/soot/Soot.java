package oscar.soot;

import oscar.utils.ConfigParser;
import oscar.utils.logger.LoggerFactory;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Soot {
  private static final Logger logger = LoggerFactory.getInstance(Soot.class);


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

    Options.v().set_soot_classpath(ConfigParser.TargetDirectory);
    Options.v().set_output_dir(ConfigParser.OutputDirectory);

    // Check if JAR or class file and process accordingly
    if (targetFileType == FILE_TYPE.JAR) {
      Options.v().set_output_jar(true);
      Options.v().set_process_dir(List.of(ConfigParser.TargetDirectory));
    }

    if (targetFileType == FILE_TYPE.CLASS) {
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

  private enum FILE_TYPE {
    JAR,
    CLASS,
    INVALID
  }
}
