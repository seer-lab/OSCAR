package oscar.transformers.noisers;

import oscar.engine.Engine;
import oscar.transformers.CustomTransformer;
import oscar.engine.generators.JimpleGenerator;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SynchronizedBlockNoiser extends CustomTransformer {
  @Override
  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (!isClassBlacklisted(b.getMethod().getDeclaringClass().getName()))
      return;

    JimpleBody body = (JimpleBody) b;
    JimpleGenerator generator = new JimpleGenerator(body);
    UnitPatchingChain boxes = body.getUnits();

    // Find invocations of synchronized methods
    List<JInvokeStmt> syncMethodInvocations =
        boxes.stream()
             .filter(JInvokeStmt.class::isInstance)
             .map(box -> ((JInvokeStmt) box))
             .filter(box -> box.getInvokeExpr().getMethod().isSynchronized())
             .filter(box -> !box.getInvokeExpr().getMethod().getDeclaringClass().getName().equals("java.lang.Thread"))
             .collect(Collectors.toList());

    // Nothing to change, leave
    if (syncMethodInvocations.isEmpty())
      return;

    for (JInvokeStmt syncMethodInvocation : syncMethodInvocations) {
      // Create statement to insert sleep noise with a random id
      long noiseLocationID = Engine.generateLocationID();
      JimpleLocal idLocal = generator.getLocal(LongType.v());
      JAssignStmt assignStmt = new JAssignStmt(idLocal, LongConstant.v(noiseLocationID));
      Stmt noiseBeforeStmt = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void sleep(long)", List.of(idLocal));

      boxes.insertBefore(assignStmt, syncMethodInvocation);
      boxes.insertBefore(noiseBeforeStmt, syncMethodInvocation);
    }

    b.validate();
  }
}
