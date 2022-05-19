package oscar.controller.util.output;

import oscar.utils.logger.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class LazyFileOutput extends FileOutput {
  private static final Logger logger = LoggerFactory.getInstance(LazyFileOutput.class);

  private final ArrayList<String> buffer = new ArrayList<>();

  public LazyFileOutput() {
    super();
  }

  @Override
  public synchronized void write(String output) {
    logger.fine("Writing to buffer.");

    if (output == null)
      throw new RuntimeException("Null string fed to file output.");

    buffer.add(output + "\n");
  }

  @Override
  public void terminate() {
    for (String s : buffer) {
      if (s == null)
        throw new RuntimeException("Null string fed to file output buffer.");

      try {
        writer.write(s);
      } catch (IOException e) {
        throw new RuntimeException("Failed to write to file '" + filepath + "'.", e);
      }
    }

    if (writer != null)
      try {
        writer.flush();
        writer.close();
      } catch (IOException e) {
        throw new RuntimeException("Failed to close writer.");
      }
  }
}
