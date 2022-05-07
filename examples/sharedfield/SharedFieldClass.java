public class SharedFieldClass {
  public int pubInt = 0;
  private int privInt = 0;

  public static int staticPubInt = 0;
  private static int staticPrivInt = 0;

  public SharedFieldClass() {}

  public void doRoutine() {
    pubInt = 2;
    privInt = 2;
    staticPubInt = 2;
    staticPrivInt = 2;

    privInt = 2;
  }

  public int getPubInt() {
    return pubInt;
  }

  public int getPrivInt() {
    return privInt;
  }

  public static int getStaticPubInt() {
    return staticPubInt;
  }

  public static int getStaticPrivInt() {
    return staticPrivInt;
  }
}
