import java.util.List;

public class PrintIDThread {
  public static void main(String[] args) throws InterruptedException {
    Thread t = new Thread(new Routine());

    t.start();

    Thread.sleep(1000);
  }

  static class Routine implements Runnable {
    @Override
    public void run() {
      System.out.println("I am running.");
    }
  }
}
