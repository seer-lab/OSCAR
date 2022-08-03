import java.util.UUID;

public class Clazz {
  public int C = 2;
  public int D = 2;
  public int X = 2;

  public int E = 2;
  public int G = 2;

  public Clazz() {}

  public int meth2() {
    if (UUID.randomUUID().toString().equals("a"))
      return E;
    else
      return G;
  }
}