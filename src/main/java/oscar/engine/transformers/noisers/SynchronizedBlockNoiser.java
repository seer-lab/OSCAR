package oscar.engine.transformers.noisers;

import oscar.engine.transformers.CustomTransformer;
import oscar.engine.writers.SleepWriter;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JInvokeStmt;

import java.util.List;
import java.util.Map;

public class SynchronizedBlockNoiser extends CustomTransformer {
  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (!isClassBlacklisted(b.getMethod().getDeclaringClass().getName()))
      return;

    JimpleBody body = (JimpleBody) b;
    SootMethod bodyMethod = body.getMethod();


    InvokeStmt sleepInstructionStmt = SleepWriter.createStatement(4000L);

    UnitPatchingChain boxes = body.getUnits();

    // Find invocations of synchronized methods
    List<JInvokeStmt> synchronizedMethodInvocations =
        boxes.stream()
             .filter(JInvokeStmt.class::isInstance)
             .map(box -> ((JInvokeStmt) box))
             .filter(box -> box.getInvokeExpr().getMethod().isSynchronized())
             .toList();

    // Nothing to change, leave
    if (boxes.isEmpty())
      return;

    // Add InterruptedException exception to signature for sleeps
    SootClass interruptedException = Scene.v().getSootClass("java.lang.InterruptedException");
    bodyMethod.addExceptionIfAbsent(interruptedException);









    b.validate();
  }
}
