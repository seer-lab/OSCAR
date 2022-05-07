public class VolatileThread implements Runnable {
  public VolatileThread() {}

  @Override
  public void run() {
    System.out.println(Volatile.A);
    System.out.println(Volatile.B);

    Volatile.B++;
    Volatile.A++;
  }
}
