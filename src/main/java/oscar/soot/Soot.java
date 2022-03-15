package oscar.soot;

import oscar.utils.ConfigParser;
import oscar.utils.error.CustomRuntimeException;
import oscar.utils.logger.LoggerFactory;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.util.List;
import java.util.logging.Logger;

public class Soot {
  private static final Logger logger = LoggerFactory.getInstance(Soot.class);

  public static Scene initializeScene() {
    G.reset();

    logger.info("Initializing Soot engine.");
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_whole_program(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_validate(true);
    Options.v().set_include_all(true);

    Options.v().set_soot_classpath(ConfigParser.TargetDirectory);
    Options.v().set_output_dir(ConfigParser.OutputDirectory);

    // Check if JAR or class file and process accordingly
    if (ConfigParser.TargetFile.endsWith(".jar")) {
      Options.v().set_output_format(Options.output_format_J);
      Options.v().set_process_dir(List.of(ConfigParser.TargetDirectory));
    } else if (ConfigParser.TargetFile.endsWith(".class")) {
      Options.v().set_output_format(Options.output_format_class);
      SootClass sc = Scene.v().loadClassAndSupport(ConfigParser.MainClass);
      sc.setApplicationClass();
    } else
      throw new CustomRuntimeException(logger, "Invalid target file type.");

    Scene.v().loadNecessaryClasses();

    logger.info("Soot engine initialization complete.");

    return Scene.v();

    // Options.v().set_src_prec(Options.src_prec_java);
    // Options.v().set_android_jars(androidJar);
    //Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
    //Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
    //Options.v().set_process_multiple_dex(true);
  }
}
