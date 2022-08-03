import java.util.Random;
import java.util.UUID;

public class SharedConditionals {
  public static int A = 1;
  public static int B = 2;

  public static void main(String[] args) throws InterruptedException {
    if (1 > SharedConditionals.B) {
      System.out.println();
      SharedConditionals.A = 5;
      System.out.println();
    } else
      SharedConditionals.A = 2;
  }
}
