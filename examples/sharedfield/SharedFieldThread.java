public class SharedFieldThread implements Runnable {
  SharedFieldClass b;

  public SharedFieldThread(SharedFieldClass clazz, int i) {
    b = clazz;
  }

  @Override
  public void run() {
    SharedFieldClass a = new SharedFieldClass();

    a.doRoutine();

    SharedFieldClass.staticPubInt = (int) Thread.currentThread().getId();

    b.getPrivInt();

    Runnable r = a::doRoutine;

    SharedFieldClass c = new SharedFieldClass();

    someFunc1(c);

    r.run();
  }

  public static int someFunc1(SharedFieldClass c) {
    return c.getPrivInt();
  }
}
