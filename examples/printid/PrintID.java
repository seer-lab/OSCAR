import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class PrintID {
  public static void main(String[] args) throws InterruptedException {
    ArrayList<Thread> threads = new ArrayList<>();

    int nThreads = 5;

    if (args.length > 0)
      nThreads = Integer.parseInt(args[0]);

    for (int i = 0; i < nThreads; i++)
      threads.add(new Thread(PrintID::routine));

    for (Thread t : threads)
      t.start();

    for (Thread t : threads)
      t.join();
  }

  private static void routine() {
    new ReentrantLock().lock();
    System.out.println(Thread.currentThread().getId());
  }
}
