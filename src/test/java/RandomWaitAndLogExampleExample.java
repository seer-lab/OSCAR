import java.util.Random;

public class RandomWaitAndLogExampleExample {
  public static void main(String[] args) throws InterruptedException {
    long waitTime = new Random().nextLong(4000L);
    System.out.printf("Waiting for %s%n", waitTime);
    Thread.sleep(waitTime);
  }
}
