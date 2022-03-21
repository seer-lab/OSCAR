package oscar.engine.transformers.noisers;

import oscar.engine.transformers.CustomTransformer;
import oscar.engine.writers.WriterUtils;
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

    // Add a new local for new java.util.Random value
    SootClass randomClass = Scene.v().getSootClass("java.util.Random");
    Local randomLocal = WriterUtils.generateLocal(RefType.v(randomClass));
    JNewExpr newRandomExp = new JNewExpr(RefType.v(randomClass));
    JAssignStmt randomCreationStmt = new JAssignStmt(randomLocal, newRandomExp);

    boxes.addFirst(randomCreationStmt);

    // Invoke the init
    SootMethod randomInitMethod = randomClass.getMethod("<init>");
    JSpecialInvokeExpr randomInitStmt = new JSpecialInvokeExpr(randomLocal, randomInitMethod.makeRef(), List.of());

    // Add new local for the random value
    JimpleLocal randomValueLocal = WriterUtils.generateLocal(LongType.v());
    SootMethod randomNextLongMethod = randomClass.getMethod("nextLong");
    LongConstant randomLongConstant = LongConstant.v(4000L);
    JVirtualInvokeExpr randomNextInvoke = new JVirtualInvokeExpr(randomValueLocal, randomNextLongMethod.makeRef(), List.of(randomLongConstant));

    ////////////////////////////////////////////////////////////////////////////

    // Print random length
    JimpleLocal printLocal = WriterUtils.generateLocal(RefType.v("java.io.PrintStream"));
    SootField sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>");
    JAssignStmt sysOutAssignStmt = new JAssignStmt(printLocal, Jimple.v().newStaticFieldRef(sysOutField.makeRef()));

    // Create array for print parameters
    JimpleLocal arrayLocal = WriterUtils.generateLocal(RefType.v("java.lang.Object"));
    JAssignStmt printArray = new JAssignStmt(arrayLocal, IntConstant.v(1));

    // Get long value to string
    JimpleLocal longToStringLocal = WriterUtils.generateLocal(LongType.v());
    SootClass longClass = Scene.v().getSootClass("java.lang.long");
    SootMethod longValueOf = longClass.getMethod("valueOf");
    JStaticInvokeExpr valueOfLong = new JStaticInvokeExpr(longValueOf.makeRef(), List.of(randomValueLocal));
    JAssignStmt valueOfLongAssignment = new JAssignStmt(longToStringLocal, valueOfLong);

    // Assign value of long to array
    JArrayRef arrayForPrint = new JArrayRef(arrayLocal, IntConstant.v(0));
    JAssignStmt longToArrayAssignment = new JAssignStmt(arrayForPrint, longToStringLocal);

    // Print
    SootClass printStreamClass = Scene.v().getSootClass("java.io.PrintStream");
    SootMethod printStreamMethod = printStreamClass.getMethod("printf");
    StringConstant printMsg = StringConstant.v("Waiting for %s%n");
    JVirtualInvokeExpr printInvoke =
        new JVirtualInvokeExpr(printLocal, printStreamMethod.makeRef(), List.of(printMsg, arrayLocal));

    // Sleep
    SootClass threadClass = Scene.v().getSootClass("java.lang.Thread");
    SootMethod sleepMethod = threadClass.getMethod("sleep");
    JStaticInvokeExpr sleepInvoke = new JStaticInvokeExpr(sleepMethod.makeRef(), List.of(randomValueLocal));

    b.validate();
  }
}
