public class SimpleSleepExampleClass {
  public SimpleSleepExampleClass() {

  }

  public void hello() {
    synchronized (this) {
      System.out.println("Hello");
    }
  }
}
