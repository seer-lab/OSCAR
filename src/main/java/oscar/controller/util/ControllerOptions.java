package oscar.controller.util;

import oscar.Main;
import oscar.controller.noise.NoisePlacement;
import oscar.controller.noise.NoiseCategory;
import oscar.controller.util.output.*;
import oscar.utils.logger.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ControllerOptions {
  private static final Logger logger = LoggerFactory.getInstance(ControllerOptions.class);

  public static final List<ControllerOption> CONTROLLER_OPTIONS = Arrays.asList(
      new ControllerOption("InjectedArgs", "Inject arguments into the program", "String", "", "-a", "--args"),
      new ControllerOption("ConfigFile", "Set config file location to load", "String", "", "-c", "--config_file"),
      new ControllerOption("ConsoleOutput", "Enable output of noising locations signals to console", "Flag", "False", "-co", "--console-output"),
      new ControllerOption("FileOutput", "Enable output of noising locations signals to a file", "String", "", "-fo", "--file-output"),
      new ControllerOption("LazyFileOutput", "Enable lazy output of noising locations signals to a file", "String", "", "-lfo", "--lazy-file-output"),
      new ControllerOption("MaxSleepLength", "Set maximum sleep length", "Long", "0", "-M", "--max_sleep_length"),
      new ControllerOption("MinSleepLength", "Set minimum sleep length", "Long", "400", "-m", "--min_sleep_length"),
      new ControllerOption("DisableNoise", "Disable all noise", "Flag", "False", "-d", "--disable-noise"),
      new ControllerOption("NoisePlacements", "Set the list of active noise placement types.", "List<String>", "All", "-np", "--noise-placements"),
      new ControllerOption("NoiseCategories", "Set the list of active noise placement categories.", "List<String>", "All", "-np", "--noise-categories"),
      new ControllerOption("PrintNoisePlacements", "Print all possible noise placements.", "Flag", "-", "-pnp", "--print-noise-placements"),
      new ControllerOption("Version", "Print OSCAR version.", "Flag", "-", "-v", "--version"),
      new ControllerOption("Verbose", "Enable full logging.", "Flag", "False", "-vb", "--verbose"),
      new ControllerOption("Quiet", "Disable logging.", "Flag", "False", "-q", "--quiet"),
      new ControllerOption("Help", "Print Help.", "Flag", "False", "-h", "--help")
  );

  public String InjectedArgs = "";
  public String ConfigFile = null;
  public ControllerOutput ControllerOutput = null;
  public Long MaxSleepLength = 400L;
  public Long MinSleepLength = 0L;
  public boolean DisableNoise = false;
  public final HashSet<NoisePlacement> NoisePlacements = NoisePlacement.getAll();
  public final HashSet<NoiseCategory> NoiseCategories = NoiseCategory.getAll();

  public boolean Verbose = false;
  public boolean Quiet = false;

  public static ControllerOptions parse(String[] argv) {
    ControllerOptions options = new ControllerOptions();

    for (int i = 0; i < argv.length; i++) {
      String arg = argv[i];

      List<ControllerOption> matchingArgs = CONTROLLER_OPTIONS.stream()
                                                              .filter(c -> c.matchesAlias(arg))
                                                              .collect(Collectors.toList());

      if (matchingArgs.size() > 1)
        throw new RuntimeException("Argument '" + arg + "' matched  more than one option.");

      if (matchingArgs.size() == 0)
        throw new RuntimeException("Argument '" + arg + "' matches no known options, use --help or -h.");

      switch (matchingArgs.get(0).getName()) {
        case "InjectedArgs":
          options.InjectedArgs = argv[i + 1];
          i++;
          break;
        case "ConfigFile":
          options.ConfigFile = argv[i + 1];
          ControllerConfigFile.readFile(options);
          i++;
          break;
        case "FileOutput":
          if (options.ControllerOutput != null)
            throw new RuntimeException("Output method already set.");

          options.ControllerOutput = new RegularFileOutput();
          i++;
          break;
        case "LazyFileOutput":
          if (options.ControllerOutput != null)
            throw new RuntimeException("Output method already set.");

          options.ControllerOutput = new LazyFileOutput();
          i++;
          break;
        case "ConsoleOutput":
          if (options.ControllerOutput != null)
            throw new RuntimeException("Output method already set.");

          options.ControllerOutput = new ConsoleOutput();
          break;
        case "MaxSleepLength":
          options.MaxSleepLength = parseLong(argv[i + 1]);
          i++;
          if (options.MaxSleepLength < 0)
            throw new RuntimeException("Invalid value for 'max_sleep_length', must be bigger than 0.");
          break;
        case "MinSleepLength":
          options.MinSleepLength = parseLong(argv[i + 1]);
          i++;

          if (options.MinSleepLength < 0)
            throw new RuntimeException("Invalid value for 'min_sleep_length', must be bigger than 0.");
          break;
        case "NoisePlacements":
          options.NoisePlacements.clear();

          // Read all noise placements
          while (i + 1 < argv.length && !argv[i + 1].contains("-"))
            options.NoisePlacements.add(NoisePlacement.fromString(argv[++i]));
          break;
        case "NoiseCategories":
          options.NoiseCategories.clear();

          // Read all noise placement categories
          while (i + 1 < argv.length && !argv[i + 1].contains("-"))
            options.NoiseCategories.add(NoiseCategory.fromString(argv[++i]));
          break;
        case "PrintNoisePlacements":
          printNoiseLocations();
          System.exit(0);
          break;
        case "DisableNoise":
          options.DisableNoise = true;
          break;
        case "Verbose":
          if (options.Quiet)
            continue;

          logger.info("Setting logger level to verbose (FINEST).");
          LoggerFactory.setLevel(Level.FINEST);
          break;
        case "Quiet":
          if (options.Verbose)
            continue;

          logger.info("Setting logger level to quiet (OFF).");
          LoggerFactory.setLevel(Level.OFF);
          break;
        case "Version":
          System.out.println("OSCAR " + Main.VERSION);
          System.exit(0);
          break;

        case "Help":
          printHelp();
          System.exit(0);
          break;
      }
    }

    if (options.MinSleepLength > options.MaxSleepLength)
      throw new RuntimeException("Minimum sleep length should be lower than maximum.");
    return options;
  }

  private static void printHelp() {
    System.out.println("Usage:");
    System.out.println("\tjava [java_options] <mainclass> [oscar_options]");
    System.out.println("\t\t(to execute a class)");
    System.out.println("\tor: java -jar <mainclass> [oscar_options]");
    System.out.println("\t\t(to execute a jar file)");

    System.out.println();
    System.out.println("OSCAR options include:");

    for (ControllerOption option : CONTROLLER_OPTIONS)
      System.out.printf(
          "\t%-25s\t%-15s\t%-10s\t%s\n",
          option.getAliasesString(),
          option.getType(),
          option.getDefaultVal(),
          option.getDescription()
      );

    System.out.println();

    System.out.println("OSCAR Noise Injector 2022");
  }

  private static void printNoiseLocations() {
    System.out.println("Possible noising locations:");

    System.out.printf(
        "\t%-25s\t%-25s\t%-25s\n",
        "Type",
        "Name",
        "Shorthand code"
    );

    System.out.printf(
        "\t%-25s\t%-25s\t%-25s\n",
        "-------------------------",
        "-------------------------",
        "-------------------------"
    );

    for (NoisePlacement np : NoisePlacement.values())
      System.out.printf(
          "\t%-25s\t%-25s\t%-25s\n",
          np.getCategory().name().replace("_", " "),
          np.name().replace("_", " "),
          np.getShorthand()
      );
  }

  private static int parseInt(String arg) {
    try {
      return Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid argument for option '" + arg + "', expected an integer.");
    }
  }

  private static long parseLong(String arg) {
    try {
      return Long.parseLong(arg);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid argument for option '" + arg + "', expected a long.");
    }
  }
}
