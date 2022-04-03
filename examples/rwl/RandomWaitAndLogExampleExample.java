package resources.examples;

import java.util.Random;

public class RandomWaitAndLogExampleExample {
  public static void main(String[] args) throws InterruptedException {
    long waitTime = new Random().nextLong();
    waitTime = Math.abs(waitTime % 4000L);
    System.out.printf("Waiting for %s%n", waitTime);
    Thread.sleep(waitTime);
  }
}
