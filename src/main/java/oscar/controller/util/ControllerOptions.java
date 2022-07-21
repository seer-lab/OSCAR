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

  private static final List<ControllerOption> CONTROLLER_OPTIONS = Arrays.asList(
      new ControllerOption("InjectedArgs", "Inject arguments into the program", "String", "", "-a", "--args"),
      // new ControllerOption("ConfigFile", "Set config file location to load", "String", "", "-c", "--config_file"),
      new ControllerOption("ConsoleOutput", "Enable output of noising locations signals to console", "Flag", "False", "-co", "--console-output"),
      new ControllerOption("FileOutput", "Enable output of noising locations signals to a file", "Flag", "False", "-fo", "--file-output"),
      new ControllerOption("LazyFileOutput", "Enable lazy output of noising locations signals to a file", "Flag", "False", "-lfo", "--lazy-file-output"),
      new ControllerOption("MaxNoiseIntensity", "Set maximum noise intensity", "Long", "10", "-M", "--max_noise_intensity"),
      new ControllerOption("MinNoiseIntensity", "Set minimum noise intensity", "Long", "0", "-m", "--min_noise_intensity"),
      new ControllerOption("DisableNoise", "Disable all noise", "Flag", "False", "-d", "--disable-noise"),
      new ControllerOption("DisableNoiseTracing", "Disable all noise tracing", "Flag", "False", "-dt", "--disable-tracing"),
      new ControllerOption("DisablePreNoiseTracing", "Disable pre-noise tracing", "Flag", "False", "-d1", "--disable-pre-noise-trace"),
      new ControllerOption("DisablePostNoiseTracing", "Disable post-noise tracing", "Flag", "False", "-d2", "--disable-post-noise-trace"),
      new ControllerOption("YieldMode", "Set noise type to yield.", "Flag", "False", "-y", "--yield"),
      new ControllerOption("NoisePlacements", "Set the list of active noise placements.", "List<String>", "All", "-np", "--noise-placements"),
      new ControllerOption("NoiseCategories", "Set the list of active noise categories.", "List<String>", "All", "-nc", "--noise-categories"),
      new ControllerOption("PrintNoisePlacements", "Print all possible noise placements.", "Flag", "-", "-pnp", "--print-noise-placements"),
      new ControllerOption("Version", "Print OSCAR version.", "Flag", "-", "-v", "--version"),
      new ControllerOption("Verbose", "Enable full logging.", "Flag", "False", "-vb", "--verbose"),
      new ControllerOption("Quiet", "Disable logging.", "Flag", "False", "-q", "--quiet"),
      new ControllerOption("Help", "Print Help.", "Flag", "False", "-h", "--help")
  );

  public String InjectedArgs = "";
  public String ConfigFile = null;
  public ControllerOutput ControllerOutput = null;
  public Long MaxNoiseIntensity = 10L;
  public Long MinNoiseIntensity = 0L;
  public boolean DisableNoise = false;
  public boolean DisablePostNoiseTracing = false;
  public boolean DisablePreNoiseTracing = false;
  public final HashSet<NoisePlacement> NoisePlacements = NoisePlacement.getAll();
  public final HashSet<NoiseCategory> NoiseCategories = NoiseCategory.getAll();

  public boolean Verbose = false;
  public boolean YieldMode = false;
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
          break;
        case "LazyFileOutput":
          if (options.ControllerOutput != null)
            throw new RuntimeException("Output method already set.");

          options.ControllerOutput = new LazyFileOutput();
          break;
        case "ConsoleOutput":
          if (options.ControllerOutput != null)
            throw new RuntimeException("Output method already set.");

          options.ControllerOutput = new ConsoleOutput();
          break;
        case "MaxNoiseIntensity":
          if (options.DisableNoise)
            throw new RuntimeException("Noise disabled.");

          options.MaxNoiseIntensity = parseLong(argv[i + 1]);
          i++;
          if (options.MaxNoiseIntensity < 0)
            throw new RuntimeException("Invalid value for 'max_noise_intensity', must be higher or equal to 0.");

          if (options.MaxNoiseIntensity < options.MinNoiseIntensity)
            throw new RuntimeException("Invalid value for 'max_noise_intensity', must be lower or equal to min_noise_intensity.");
          break;
        case "MinNoiseIntensity":
          if (options.DisableNoise)
            throw new RuntimeException("Noise disabled.");

          options.MinNoiseIntensity = parseLong(argv[i + 1]);
          i++;

          if (options.MinNoiseIntensity < 0)
            throw new RuntimeException("Invalid value for 'min_noise_intensity', must be higher or equal to 0.");

          if (options.MinNoiseIntensity > options.MaxNoiseIntensity)
            throw new RuntimeException("Invalid value for 'min_noise_intensity', must be lower or equal to max_noise_intensity.");
          break;
        case "YieldMode":
          if (options.DisableNoise)
            throw new RuntimeException("Noise disabled.");

          options.YieldMode = true;
          break;
        case "NoisePlacements":
          options.NoisePlacements.clear();

          // Read all noise placements
          while (i + 1 < argv.length && !argv[i + 1].contains("-"))
            options.NoisePlacements.add(NoisePlacement.fromString(argv[++i]));
          break;
        case "NoiseCategories":
          options.NoiseCategories.clear();

          // Read all noise placement categories and add all their respective noise types
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
        case "DisablePreNoiseTracing":
          options.DisablePreNoiseTracing = true;
          break;
        case "DisablePostNoiseTracing":
          options.DisablePostNoiseTracing = true;
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

    if (options.MinNoiseIntensity > options.MaxNoiseIntensity)
      throw new RuntimeException("Minimum sleep length should be lower than maximum.");

    if (options.MinNoiseIntensity + options.MaxNoiseIntensity == 0)
      options.DisableNoise = true;

    // Activate all noise categories from activated noise placements and vice versa
    Arrays.stream(NoisePlacement.values())
          .filter(np -> options.NoiseCategories.contains(np.getCategory()))
          .forEach(options.NoisePlacements::add);
    options.NoisePlacements.stream().map(NoisePlacement::getCategory).forEach(options.NoiseCategories::add);

    return options;
  }

  private static void printHelp() {
    System.out.println("Usage:");
    System.out.println("\tjava [java_options] <mainclass> [oscar_controller_options]");
    System.out.println("\t\t(to execute a class)");
    System.out.println("\tor: java -jar <mainclass> [oscar_controller_options]");
    System.out.println("\t\t(to execute a jar file)");

    System.out.println();
    System.out.println("OSCAR controller gitoptions include:");

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
    System.out.println("Possible noise placements:");

    System.out.printf(
        "\t%-25s\t%-25s\n",
        "Category",
        "Shorthand code"
    );

    System.out.printf(
        "\t%-25s\t%-25s\n",
        "-------------------------",
        "-------------------------"
    );

    for (NoiseCategory nc : NoiseCategory.values())
      System.out.printf(
          "\t%-25s\t%-25s\n",
          nc.name().replace("_", " "),
          nc.getShorthand()
      );

    System.out.println();

    System.out.println("Possible noising locations:");

    System.out.printf(
        "\t%-25s\t%-25s\t%-25s\n",
        "Category",
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
