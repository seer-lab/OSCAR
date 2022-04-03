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

  @Parameter(names = {"--max_sleep_length", "-M"}, description = "Set maximum speed length")
  public Long MaxSpeedLength = 400L;

  @Parameter(names = {"--min_sleep_length", "-m"}, description = "Set minimum speed length")
  public Long MinSpeedLength = 0L;

  @Parameter(names = {"--verbose", "-v"}, description = "Enable logging.")
  public Boolean Verbose = false;

  public static ControllerArgs parse(String[] argv) {
    ControllerArgs args = new ControllerArgs();

    JCommander.newBuilder()
        .addObject(args)
        .build()
        .parse(argv);

    return args;
  }
}
