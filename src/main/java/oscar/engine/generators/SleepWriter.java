package oscar.engine.generators;

import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.jimple.*;

import java.util.List;

public final class SleepWriter {
  public static InvokeStmt createStatement(long length) {
    SootMethod sleepMethod = Scene.v().grabMethod("<java.lang.Thread: void sleep(long)>");

    Value sleepLength = LongConstant.v(length);
    StaticInvokeExpr sleepInstructionExpr = Jimple.v().newStaticInvokeExpr(sleepMethod.makeRef(), List.of(sleepLength));

    return Jimple.v().newInvokeStmt(sleepInstructionExpr);
  }
}
