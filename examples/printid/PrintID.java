import java.util.ArrayList;

public class PrintID {
  public static void main(String[] args) throws InterruptedException {
    ArrayList<Thread> threads = new ArrayList<>();

    int nThreads = 5;

    if (args.length > 0)
      nThreads = Integer.parseInt(args[0]);

    for (int i = 0; i < nThreads; i++)
      threads.add(new Thread(() -> System.out.println(Thread.currentThread().getId())));

    for (Thread t : threads)
      t.start();

    for (Thread t : threads)
      t.join();
  }
}
