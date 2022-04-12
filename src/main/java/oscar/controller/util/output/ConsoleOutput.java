package oscar.controller.util.output;

public class ConsoleOutput implements ControllerOutput {
  @Override
  public void write(String output) {
    System.out.println(output);
  }

  @Override
  public void terminate() {
    System.out.flush();
  }
}
