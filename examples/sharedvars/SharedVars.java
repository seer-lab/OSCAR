import java.util.Random;

public class SharedVars {
  public static int A = 1;
  public static int B = 2;


  public static void main(String[] args) throws InterruptedException {
    Clazz clazz = new Clazz();

    SharedVars.A = SharedVars.B;
    SharedVars.B = Math.round(clazz.C);
    clazz.C = 5 + clazz.C + 1 + 2 + 3 + new Random().nextInt();
    SharedVars.B = 5 + SharedVars.B + 1 + 2 + 3 + new Random().nextInt();
    SharedVars.A = inc(clazz.X);
    clazz.D = clazz.D + 3;
    clazz.D = Math.round(clazz.X);
  }

  private static int inc(int a) {
    return ++a;
  }

}
