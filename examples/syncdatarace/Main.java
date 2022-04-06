import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {
  private static int currData = -1;

  public static void main(String[] args) throws InterruptedException {
    int threadCount = Integer.parseInt(args[0]);

    CountDownLatch countDownLatch = new CountDownLatch(threadCount);
    Executor executor = Executors.newFixedThreadPool(16);
    System.out.println("Launching " + threadCount + " threads.");

    for (int i = 0; i < threadCount; i++)
      executor.execute(new Thread(new DRThread(i, countDownLatch)));

    System.out.println("Threads Launched.");
    System.out.print("Waiting...");

    while (countDownLatch.getCount() != 0) {
      System.out.print(".");
      java.lang.Thread.sleep(500);
    }

    System.out.println("\nData: " + currData);

    System.exit(0);
  }

  synchronized static public void setData(int data) {
    currData = data;
  }
}
