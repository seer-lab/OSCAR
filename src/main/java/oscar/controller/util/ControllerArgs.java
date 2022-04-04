package oscar.controller.util;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public final class ControllerArgs {
  @Parameter(names = {"--args", "-a"}, description = "Inject arguments into the program")
  public String InjectedArgs = "";

  @Parameter(names = {"--config_file", "-c"}, description = "Set config file location to load")
  public String ConfigFile = null;

  @Parameter(names = {"--output", "-o"}, description = "Set output file location")
  public String OutputLocation = null;

  @Parameter(names = {"--max_sleep_length", "-M"}, description = "Set maximum sleep length")
  public Long MaxSleepLength = 400L;

  @Parameter(names = {"--min_sleep_length", "-m"}, description = "Set minimum sleep length")
  public Long MinSleepLength = 0L;

  @Parameter(names = {"--verbose", "-v"}, description = "Enable full logging.")
  public Boolean Verbose = false;

  @Parameter(names = {"--quiet", "-q"}, description = "Disable logging.")
  public Boolean Quiet = false;

  public static ControllerArgs parse(String[] argv) {
    ControllerArgs args = new ControllerArgs();

    JCommander.newBuilder()
        .addObject(args)
        .build()
        .parse(argv);

    if (args.MaxSleepLength < 0)
      throw new RuntimeException("Invalid value for 'max_sleep_length', must be bigger than 0.");

    if (args.MinSleepLength < 0)
      throw new RuntimeException("Invalid value for 'min_sleep_length', must be bigger than 0.");

    return args;
  }
}
