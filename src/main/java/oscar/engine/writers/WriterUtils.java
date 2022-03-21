package oscar.engine.writers;

import soot.Body;
import soot.LongType;
import soot.Type;
import soot.javaToJimple.DefaultLocalGenerator;
import soot.jimple.internal.JimpleLocal;

public final class WriterUtils {
  private static int localCounter = 0;

  public static JimpleLocal generateLocal(Type type) {
    return new JimpleLocal("__oscar__local__%d".formatted(localCounter++), type);
  }
}
