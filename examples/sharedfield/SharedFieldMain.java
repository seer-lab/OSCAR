import java.util.ArrayList;

public class SharedFieldMain {
  public static void main(String[] args) throws InterruptedException {
    ArrayList<Thread> threads = new ArrayList<>();

    SharedFieldClass a = new SharedFieldClass();

    for (int i = 0; i < 4; i++) {
      threads.add(new Thread(new SharedFieldThread(a, 2)));
    }

    for (Thread t : threads)
      t.start();

    for (Thread t : threads)
      t.join();

    System.exit(0);
  }
}
