package oscar.controller.util;

import oscar.controller.noise.NoisePlacement;
import oscar.controller.noise.SleepNoise;
import oscar.utils.logger.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ControllerOptions {
  private static final Logger logger = LoggerFactory.getInstance(ControllerOptions.class);

  public static final List<ControllerOption> CONTROLLER_OPTIONS = Arrays.asList(
      new ControllerOption("InjectedArgs", "Inject arguments into the program", "--args", "-a"),
      new ControllerOption("ConfigFile", "Set config file location to load", "--config_file", "-c"),
      new ControllerOption("OutputLocation", "Set output file location", "--output", "-o"),
      new ControllerOption("MaxSleepLength", "Set maximum sleep length", "--max_sleep_length", "-M"),
      new ControllerOption("MinSleepLength", "Set minimum sleep length", "--min_sleep_length", "-m"),
      new ControllerOption("Verbose", "Enable full logging.", "--verbose", "-v"),
      new ControllerOption("Quiet", "Disable logging.", "--quiet", "-q"),
      new ControllerOption("Help", "Print Help.", "--help", "-h")
  );

  public String InjectedArgs = "";
  public String ConfigFile = null;
  public String OutputLocation = null;
  public Long MaxSleepLength = 400L;
  public Long MinSleepLength = 0L;

  public final ConcurrentHashMap<Long, SleepNoise> NoiseLocations = new ConcurrentHashMap<>(); // TODO Should this be a treemap?
  public final HashSet<NoisePlacement> ActiveNoisePlacements = new HashSet<>(Arrays.asList(NoisePlacement.values()));

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
        case "OutputLocation":
          options.OutputLocation = argv[i + 1];
          i++;
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
        case "Verbose":
          logger.info("Setting logger level to verbose (FINEST).");
          LoggerFactory.setLevel(Level.FINEST);
          break;
        case "Quiet":
          logger.info("Setting logger level to quiet (OFF).");
          LoggerFactory.setLevel(Level.OFF);
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

    System.out.println("");
    System.out.println("OSCAR options include:");

    for (ControllerOption option : CONTROLLER_OPTIONS)
      System.out.printf("\t%-30s%s\n", option.getAliasesString(), option.getDescription());

    System.out.println("");

    System.out.println("OSCAR Noise Injector 2022");
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
