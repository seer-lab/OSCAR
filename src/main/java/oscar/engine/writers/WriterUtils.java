package oscar.engine.writers;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.Type;
import soot.javaToJimple.DefaultLocalGenerator;

public final class WriterUtils {
  public static Local generateNewLocal(Body body, Type type) {
    DefaultLocalGenerator lg = new DefaultLocalGenerator(body);
    return lg.generateLocal(type);
  }
}
