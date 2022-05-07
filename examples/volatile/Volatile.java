import java.util.ArrayList;

public class Volatile {
  public static int A = 1;
  public static volatile int B = 2;

  public static void main(String[] args) throws InterruptedException {
    ArrayList<Thread> threads = new ArrayList<>();

    for (int i = 0; i < 4; i++) {
      threads.add(new Thread(new VolatileThread()));
    }

    for (Thread t : threads)
      t.start();

    for (Thread t : threads)
      t.join();

    System.exit(0);
  }
}
