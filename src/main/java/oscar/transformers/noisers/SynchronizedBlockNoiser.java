package oscar.transformers.noisers;

import oscar.transformers.CustomTransformer;
import oscar.engine.generators.JimpleGenerator;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
             .toList();

    // Nothing to change, leave
    if (!syncMethodInvocations.isEmpty()) {
      // Add InterruptedException exception to signature for sleeps
      generator.addExceptionToBodyMethod("java.lang.InterruptedException");

      // Instantiate a new java.util.Random class
      JimpleLocal randomClassLocal = generator.Statement.instantiateClass("java.util.Random", List.of());

      for (JInvokeStmt syncMethodInvocation : syncMethodInvocations) {
        // create new local and get random value
        LinkedList<JAssignStmt> rndGenStmts = getRandomCreationAssignments(randomClassLocal, generator);
        JimpleLocal randomValueLocal = (JimpleLocal) rndGenStmts.getLast().getLeftOp();
        rndGenStmts.forEach(stmt -> boxes.insertBefore(stmt, syncMethodInvocation));

        // Create arguments to print
        JAssignStmt jas = generator.Conversion.longValueOf(randomValueLocal);
        JimpleLocal longValueLocal = (JimpleLocal) jas.getLeftOp();
        boxes.insertBefore(jas, syncMethodInvocation);

        List<Unit> printStmts = generator.Statement.printf("Waiting for %s%n", List.of(longValueLocal));
        printStmts.forEach(s -> boxes.insertBefore(s, syncMethodInvocation));

        boxes.insertBefore(generator.Statement.sleep(randomValueLocal), syncMethodInvocation);
      }
    }

    b.validate();
  }

  private LinkedList<JAssignStmt> getRandomCreationAssignments(JimpleLocal randomLocalRef, JimpleGenerator generator) {
    LinkedList<JAssignStmt> stmts = new LinkedList<>();

    // Invoke nextLong
    JAssignStmt nextLongStmt = generator.Statement.virtualInvoke(
        randomLocalRef,
        "java.util.Random",
        "long nextLong()",
        List.of()
    );
    stmts.add(nextLongStmt);

    // Modulo random value
    JAssignStmt moduloStmt = generator.Arithmetic.modulo(nextLongStmt.getLeftOp(), LongConstant.v(4000L));
    stmts.add(moduloStmt);

    // Do math abs on value
    JAssignStmt mathAbsStmt = generator.Statement.staticInvoke(
        "java.lang.Math",
        "long abs(long)",
        List.of(moduloStmt.getLeftOp())
    );
    stmts.add(mathAbsStmt);

    return stmts;
  }
}
