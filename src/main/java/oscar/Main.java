package oscar;

import org.apache.commons.cli.ParseException;
import oscar.engine.Engine;
import oscar.transformers.noisers.SynchronizedBlockNoiser;
import oscar.utils.ConfigParser;
import oscar.utils.OptionsParser;
import oscar.utils.logger.LoggerFactory;
import soot.*;

import java.util.List;
import java.util.logging.Logger;

public class Main {
  public static void main(String[] args) {
    // Parse program CLI
    try {
      OptionsParser.parse(args);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }

    // Parse program configuration
    ConfigParser.parse(OptionsParser.PropertiesFile);
    Logger logger = LoggerFactory.getInstance(Main.class);

    // Init soot
    Engine.start();

    // Register transformers
    List<Transform> transformers = List.of(
        new Transform("jtp.sbn", new SynchronizedBlockNoiser())
    );

    transformers.forEach(PackManager.v().getPack("jtp")::add);

    // Run Soot packs (note that our transformer pack is added to the phase "jtp")
    PackManager.v().runPacks();

    // Write the result of packs in outputPath
    PackManager.v().writeOutput();

    // Finalize soot routines
    Engine.end();
  }
}