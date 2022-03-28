import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {
  private static int currData = -1;

  public static void main(String[] args) throws InterruptedException {
    int threadCount = 500;

    CountDownLatch countDownLatch = new CountDownLatch(threadCount);
    Executor executor = Executors.newFixedThreadPool(16);
    System.out.println("Launching Threads.");

    for (int i = 0; i < threadCount; i++)
      executor.execute(new Thread(new DRThread(i, countDownLatch)));

    System.out.println("Threads Launched.");
    System.out.print("Waiting...");

    while (countDownLatch.getCount() != 0) {
      System.out.print(".");
      java.lang.Thread.sleep(500);
    }

    System.out.println("\nData: " + currData);
  }

  synchronized static public void setData(int data) {
    currData = data;
  }
}
