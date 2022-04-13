public class PrintID {
  private static volatile int count;

  public static void main(String[] args) {
    int threadCount = Integer.parseInt(args[0]);

    count = threadCount;

    System.out.println("Launching " + threadCount + " threads.");

    for (int i = 0; i < threadCount; i++)
      new Thread(PrintID::printID).start();

    System.out.flush();
    System.out.close();

    while (count != 0) {
      Thread.onSpinWait();
    }

    System.exit(0);
  }

  private synchronized static void printID() {
    System.out.println(Thread.currentThread().getId());
    count--;
  }
}
