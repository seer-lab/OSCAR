public class SharedFieldThread implements Runnable {
  SharedFieldClass a;
  SharedFieldClass b;

  int i;

  public SharedFieldThread(SharedFieldClass clazz, SharedFieldClass clazz2, int i) {
    b = clazz;
    a = clazz2;
    this.i = i;
    throw new RuntimeException();
  }

  public SharedFieldThread(SharedFieldClass clazz) {
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
