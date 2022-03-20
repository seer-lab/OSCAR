package oscar.engine.transformers;

import oscar.engine.writers.SleepWriter;
import soot.*;
import soot.jimple.*;
import soot.javaToJimple.*;

import java.util.List;
import java.util.Map;

public class RandomNoiserTransformer extends CustomTransformer {

  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (!isClassBlacklisted(b.getMethod().getDeclaringClass().getName()))
      return;

    JimpleBody body = (JimpleBody) b;

    InvokeStmt sleepInstructionStmt = SleepWriter.createStatement(4000L);

    UnitPatchingChain boxes = body.getUnits();

    int c = 0;
    Unit boxToRemove = null;

    for (Unit box : boxes) {
      boxToRemove = box;

      if (box.toString().startsWith("staticinvoke"))
        break;

      c++;
    }

   // boxes.remove(boxToRemove);

   // b.getUnits().insertAfter(List.of(sleepInstructionStmt), body.getUnits().stream().toList().get(c));

   // b.validate();
  }

  public static Local generateNewLocal(Body body, Type type) {
    LocalGenerator lg = new DefaultLocalGenerator(body);
    return lg.generateLocal(type);
  }
}
