package oscar.transformers.noisers;

import oscar.transformers.CustomTransformer;
import oscar.engine.generators.JimpleGenerator;
import oscar.engine.WriterUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

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
    JimpleGenerator generator = new JimpleGenerator(body);
    UnitPatchingChain boxes = body.getUnits();

    // Find invocations of synchronized methods
    List<JInvokeStmt> syncMethodInvocations =
        boxes.stream()
             .filter(JInvokeStmt.class::isInstance)
             .map(box -> ((JInvokeStmt) box))
             .filter(box -> box.getInvokeExpr().getMethod().isSynchronized())
             .toList();

    // Nothing to change, leave
    if (!syncMethodInvocations.isEmpty()) {
      // Add InterruptedException exception to signature for sleeps
      generator.addExceptionToBodyMethod("java.lang.InterruptedException");

      // Instantiate a new java.util.Random class
      JSpecialInvokeExpr jsie = generator.Statement.instantiateClass("java.util.Random", List.of());
      JimpleLocal randomClassLocal = (JimpleLocal) jsie.getBase();
      boxes.addFirst(new JInvokeStmt(jsie));

      for (JInvokeStmt syncMethodInvocation : syncMethodInvocations) {
        // create new local and get random value
        JAssignStmt jas = callRandomNextLong(randomClassLocal, generator);
        JimpleLocal randomValueLocal = (JimpleLocal) jas.getLeftOp();
        boxes.insertBefore(jas, syncMethodInvocation);

        // Create arguments to print
        jas = getLongValueOf(randomClassLocal, generator);
        JimpleLocal longValueLocal = (JimpleLocal) jas.getLeftOp();
        boxes.insertBefore(jas, syncMethodInvocation);

        List<Unit> printStmts = generator.Statement.printf("Waiting for %s%n", List.of(longValueLocal));
        printStmts.forEach(s -> boxes.insertBefore(s, syncMethodInvocation));

        boxes.insertBefore(generator.Statement.sleep(randomValueLocal), syncMethodInvocation);
      }
    }

    b.validate();
  }

  private JAssignStmt callRandomNextLong(JimpleLocal randomLocalRef, JimpleGenerator bodyModifier) {
    return bodyModifier.Statement.invokeMethod(
        randomLocalRef,
        "java.util.Random",
        "nextLong",
        LongType.v(),
        List.of(LongConstant.v(4000L))
    );
  }

  private JAssignStmt getLongValueOf(JimpleLocal local, JimpleGenerator generator) {
    return generator.Conversion.valueOf(LongType.v(), "java.lang.long", local);
  }
}
