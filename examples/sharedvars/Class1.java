public class Class1 {
  public static void method(int a, Object obj, String str) {
    a = a + 1;
    System.out.println(a);

    str = "asfa";

  }

  public static void method2(String ex) {
    SharedVars.A = 2;
    SharedVars.B = 2;
    SharedVars.OBJ = null;
    SharedVars.STR = null;

    Object a = SharedVars.A;

    a.equals("a");
  }
}
