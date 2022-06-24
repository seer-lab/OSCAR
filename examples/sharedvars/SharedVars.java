public class SharedVars {
  public static int A = 1;
  public static volatile int B = 2;
  public static Object OBJ;
  public static String STR;

  public static void main(String[] args) throws InterruptedException {
    int a = 1;
    Object obj = null;
    String str = null;

    Class1.method(a, obj, str);

    Class1.method2("sfsa");
  }
}
