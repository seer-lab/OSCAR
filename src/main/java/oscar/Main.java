package oscar;

import org.apache.commons.cli.ParseException;
import oscar.engine.Engine;
import oscar.transformers.injectors.ControllerInjector;
import oscar.transformers.injectors.ExitCapture;
import oscar.transformers.noisers.SynchronizedBlockNoiser;
import oscar.utils.ConfigParser;
import oscar.utils.OptionsParser;
import oscar.utils.logger.LoggerFactory;
import soot.*;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Main {
  public static void main(String[] args) {
    // Parse program CLI
    try {
      OptionsParser.parse(args);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

    // Parse program configuration
    ConfigParser.parse(OptionsParser.PropertiesFile);
    Logger logger = LoggerFactory.getInstance(Main.class);

    // Init soot
    Engine.start();

    // Register transformers
    List<Transform> transformers = Arrays.asList(
        new Transform("jtp.oci", new ControllerInjector()),
        new Transform("jtp.oec", new ExitCapture()),
        new Transform("jtp.sbn", new SynchronizedBlockNoiser())
    );

    transformers.forEach(PackManager.v().getPack("jtp")::add);

    logger.info("Running Soot packs.");

    // Run Soot packs (note that our transformer pack is added to the phase "jtp")
    PackManager.v().runPacks();

    logger.info("Writing Soot output.");

    // Write the result of packs in outputPath
    PackManager.v().writeOutput();

    logger.info("Finishing.");
    // Finalize soot routines
    Engine.end();

    System.exit(0);
  }
}