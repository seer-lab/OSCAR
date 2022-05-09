package oscar;

import oscar.controller.util.ControllerOption;
import oscar.engine.Engine;
import oscar.utils.logger.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Main {
  public static final String VERSION = "0.0.2";

  private static final List<ControllerOption> OPTIONS = Arrays.asList(
      new ControllerOption("Verbose", "Enable full logging.", "Flag", "False", "-v", "--verbose")
  );

  public static void main(String[] args) {
    if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
      System.out.println("Usage:");
      System.out.println("\toscar <targetfile> <mainclass> <outputdirectory>");
      System.out.println("OSCAR options include:");
      for (ControllerOption option : OPTIONS)
        System.out.printf(
            "\t%-25s\t%-15s\t%-10s\t%s\n",
            option.getAliasesString(),
            option.getType(),
            option.getDefaultVal(),
            option.getDescription()
        );

      System.exit(0);
    }

    if (args.length < 3 || args.length > 4) {
      System.out.println("Invalid number of arguments, expected at least 3. Use --help or -h for help.");
      System.exit(1);
    }

    if (args.length == 4 && (args[3].equals("-v") || args[3].equals("-verbose"))) {
      LoggerFactory.setLevel(Level.ALL);
    } else {
      System.out.println("Invalid value for argument 'Verbose'.");
      System.exit(1);
    }

    String targetFile = args[0];
    String mainClass = args[1];
    String outputDirectory = args[2];

    // Init soot
    Engine engine = new Engine(targetFile, mainClass, outputDirectory);
    engine.run();

    System.exit(0);
  }
}