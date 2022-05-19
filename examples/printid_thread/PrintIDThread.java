import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class PrintIDThread {
  public static void main(String[] args) throws InterruptedException {
    ArrayList<Thread> threads = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Thread t = new Thread(PrintIDThread::routine);
      threads.add(t);
      t.start();
    }

    for (Thread t : threads)
      t.join();
  }

  private static void routine() {
    ArrayList<Thread> threads = new ArrayList<>();

    System.out.println(Thread.currentThread().getId());

    for (int i = 0; i < 2; i++) {
      Thread t = new Thread(System.out::println);
      threads.add(t);
      t.start();
    }

    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
