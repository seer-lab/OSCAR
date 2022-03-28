import java.util.concurrent.CountDownLatch;

public class DRThread implements Runnable {
  private final int id;
  private final CountDownLatch countDownLatch;

  public DRThread(int id, CountDownLatch countDownLatch) {
    this.id = id;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    if (id == 1) {
      try {
        java.lang.Thread.yield();
        java.lang.Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Main.setData(id);
    countDownLatch.countDown();
  }
}
