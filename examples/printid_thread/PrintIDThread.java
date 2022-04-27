import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class PrintIDThread {
  public static void main(String[] args) throws InterruptedException {
    ArrayList<Thread> threads = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      Thread t = new Thread(PrintIDThread::routine);
      threads.add(t);
      t.start();
    }

    for (Thread t : threads)
      t.join();
  }

  private static void routine() {
    System.out.println(Thread.currentThread().getId());
  }
}
