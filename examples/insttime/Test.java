public class Test {
  public static void main(String[] args) {
    int iterations = Integer.parseInt(args[0]);

    for (int i = 0; i < 10000000; i++)
      for (int j = 0; j < iterations; j++)
        doNothing();
  }

  static synchronized void doNothing() {
    return;
  }
}
