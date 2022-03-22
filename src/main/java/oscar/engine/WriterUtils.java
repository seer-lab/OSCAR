package oscar.engine;

import soot.*;
import soot.jimple.internal.JimpleLocal;

public final class WriterUtils {
  private static int localCounter = 0;

  public static JimpleLocal generateLocal(Type type) {
    return new JimpleLocal("__oscar__local__%d".formatted(localCounter++), type);
  }

  public static void addExceptionToBodyMethod(Body body, String exceptionClass) {
    SootClass interruptedException = Scene.v().getSootClass(exceptionClass);
    body.getMethod().addExceptionIfAbsent(interruptedException);
  }
}
