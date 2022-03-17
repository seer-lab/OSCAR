package oscar.soot;

import oscar.utils.ConfigParser;
import soot.*;
import soot.jimple.*;
import soot.javaToJimple.*;
import soot.jimple.internal.JInvokeStmt;

import java.util.List;
import java.util.Map;

public class RandomNoiserTransformer extends CustomTransformer {

  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (!isClassBlacklisted(b.getMethod().getDeclaringClass().getName()))
      return;

    if (!b.getMethod().getName().equals("main"))
      return;

    JimpleBody body = (JimpleBody) b;

    SootMethod sleepMethod = Scene.v().grabMethod("<java.lang.Thread: void sleep(long)>");
    Value sleepLength = LongConstant.v(4000);
    StaticInvokeExpr sleepInstructionExpr = Jimple.v().newStaticInvokeExpr(sleepMethod.makeRef(), List.of(sleepLength));
    InvokeStmt sleepInstructionStmt = Jimple.v().newInvokeStmt(sleepInstructionExpr);

    UnitPatchingChain boxes = body.getUnits();

    int c = 0;
    Unit boxToRemove = null;

    for (Unit box : boxes) {
      boxToRemove = box;

      if (box.toString().startsWith("staticinvoke"))
        break;

      c++;
    }

    boxes.remove(boxToRemove);

    b.getUnits().insertAfter(List.of(sleepInstructionStmt), body.getUnits().stream().toList().get(c));

    b.validate();
  }


  public static Local generateNewLocal(Body body, Type type) {
    LocalGenerator lg = new DefaultLocalGenerator(body);
    return lg.generateLocal(type);
  }

}
