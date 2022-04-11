public class PrintID {

  public static void main(String[] args) {
    int threadCount = Integer.parseInt(args[0]);

    System.out.println("Launching " + threadCount + " threads.");

    for (int i = 0; i < threadCount; i++)
      new Thread(PrintID::printID).start();

    System.out.flush();
    System.out.close();

    System.exit(0);
  }

  private static void printID() {
    synchronized (PrintID.class) {
      System.out.println(Thread.currentThread().getId());
    }
  }
}
