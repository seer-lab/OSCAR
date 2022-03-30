package oscar.engine.generators;

import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.*;

import java.util.ArrayList;
import java.util.List;

public class StatementGenerator {
  private final JimpleGenerator generator;

  public StatementGenerator(JimpleGenerator generator) {
    this.generator = generator;
  }
  
  public JimpleLocal instantiateClass(String className, List<Value> initArgs) {
    // Add a new local
    SootClass sootClass = generator.getSootClass(className);
    JimpleLocal local = generator.generateLocalFromClass(sootClass);
    JNewExpr newExp = new JNewExpr(RefType.v(sootClass));

    // Invoke the init
    SootMethod initMethod = sootClass.getMethod("void <init>()");
    JSpecialInvokeExpr classInvokeExpr = new JSpecialInvokeExpr(local, initMethod.makeRef(), initArgs);

    generator.appendUnit(new JInvokeStmt(classInvokeExpr));
    generator.appendUnit(new JAssignStmt(local, newExp));

    return (JimpleLocal) classInvokeExpr.getBase();
  }

  public JAssignStmt virtualInvoke(JimpleLocal refLocal, String methodClass, String methodName, List<Value> args) {
    SootClass sootClass = generator.getSootClass(methodClass);
    SootMethod method = sootClass.getMethod(methodName);
    JimpleLocal resultLocal = generator.getLocal(method.getReturnType());

    JVirtualInvokeExpr invokeExpr =
        new JVirtualInvokeExpr(refLocal, method.makeRef(), args);

    return new JAssignStmt(resultLocal, invokeExpr);
  }

  public Stmt staticInvoke(String methodClass, String methodName, List<Value> args) {
    SootClass sootClass = generator.getSootClass(methodClass);
    SootMethod method = sootClass.getMethod(methodName);

    JStaticInvokeExpr invokeExpr =
        new JStaticInvokeExpr(method.makeRef(), args);

    if (methodName.startsWith("void"))
      return new JInvokeStmt(invokeExpr);
    else {
      JimpleLocal resultLocal = generator.getLocal(method.getReturnType());
      return new JAssignStmt(resultLocal, invokeExpr);
    }
  }

  public List<Unit> printf(String message, List<Value> args) {
    ArrayList<Unit> statements = new ArrayList<>();

    // Print the random length
    JimpleLocal printLocal = generator.getLocal(RefType.v("java.io.PrintStream"));
    SootField sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>");
    statements.add(new JAssignStmt(printLocal, Jimple.v().newStaticFieldRef(sysOutField.makeRef())));

    // Create array for print parameters
    JAssignStmt arrayCreationStmt = generator.array("java.lang.Object", 1, args.size());
    JimpleLocal arrayLocal = (JimpleLocal) arrayCreationStmt.getLeftOp();
    statements.add(arrayCreationStmt);

    // Assign args values to array
    for (int i = 0; i < args.size(); i++) {
      JArrayRef arrayForPrint = new JArrayRef(arrayLocal, IntConstant.v(i));
      statements.add(new JAssignStmt(arrayForPrint, args.get(i)));
    }

    // Print
    SootClass printStreamClass = Scene.v().getSootClass("java.io.PrintStream");
    SootMethod printStreamMethod = printStreamClass.getMethod("java.io.PrintStream printf(java.lang.String,java.lang.Object[])");
    StringConstant printMsg = StringConstant.v(message);
    JVirtualInvokeExpr printInvokeExpr =
        new JVirtualInvokeExpr(printLocal, printStreamMethod.makeRef(), List.of(printMsg, arrayLocal));
    statements.add(new JInvokeStmt(printInvokeExpr));

    return statements;
  }

  public JInvokeStmt sleep(JimpleLocal sleepLengthLocal) {
    SootClass threadClass = Scene.v().getSootClass("java.lang.Thread");
    SootMethod sleepMethod = threadClass.getMethod("void sleep(long)");

    return new JInvokeStmt(new JStaticInvokeExpr(sleepMethod.makeRef(), List.of(sleepLengthLocal)));
  }
}
