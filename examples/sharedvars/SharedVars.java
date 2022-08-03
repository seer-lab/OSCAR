import java.util.Random;
import java.util.UUID;

public class SharedVars {
  public static int A = 1;
  public static int B = 2;
  public static int Z = 2;
  public static int Y = 2;
  public static int W = 2;


  public static void main(String[] args) throws InterruptedException {
    Clazz clazz = new Clazz();

    SharedVars.A = SharedVars.B;
    SharedVars.B = Math.round(clazz.C);
    clazz.C = 5 + clazz.C + 1 + 2 + 3 + new Random().nextInt();
    SharedVars.B = 5 + SharedVars.B + 1 + 2 + 3 + new Random().nextInt();
    SharedVars.A = inc(clazz.X);
    clazz.D = clazz.D + 3;
    clazz.D = Math.round(Math.round(clazz.X));
    SharedVars.Y = meth();
    SharedVars.W = clazz.meth2();
  }

  private static int inc(int a) {
    return ++a;
  }

  private static int meth() {
    if (UUID.randomUUID().toString().equals("a"))
      return SharedVars.Z;
    else
      return SharedVars.A;
  }
}
