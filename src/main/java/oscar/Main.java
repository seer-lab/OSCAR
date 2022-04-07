package oscar;

import oscar.engine.Engine;
import oscar.transformers.injectors.ControllerInjector;
import oscar.transformers.injectors.ExitCapture;
import oscar.transformers.noisers.SynchronizedBlockNoiser;

public class Main {
  public static void main(String[] args) {
    if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
      System.out.println("Usage: oscar <targetfile> <mainclass> <outputdirectory>");
      System.exit(0);
    }
    if (args.length != 3) {
      System.out.println("Invalid number of arguments, expected 3. Use --help or -h for help.");
      System.exit(1);
    }
    String targetFile = args[0];
    String mainClass = args[1];
    String outputDirectory = args[2];

    // Init soot
    Engine engine = new Engine(targetFile, mainClass, outputDirectory);

    // Add packs and run
    engine.registerTransformer(new ControllerInjector(mainClass));
    engine.registerTransformer(new ExitCapture(mainClass));
    engine.registerTransformer(new SynchronizedBlockNoiser(mainClass));

    engine.run();

    System.exit(0);
  }
}