package oscar.engine.generators;

import oscar.engine.WriterUtils;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;
import soot.jimple.internal.*;

import java.util.ArrayList;
import java.util.List;

public record StatementGenerator(JimpleGenerator generator) {
  public JSpecialInvokeExpr instantiateClass(String className, List<Value> initArgs) {
    // Add a new local
    SootClass sootClass = generator.getSootClass(className);
    JimpleLocal local = generator.generateLocalFromClass(sootClass);
    JNewExpr newExp = new JNewExpr(RefType.v(sootClass));
    generator.appendUnit(new JAssignStmt(local, newExp));

    // Invoke the init
    SootMethod initMethod = sootClass.getMethod("<init>");
    return new JSpecialInvokeExpr(local, initMethod.makeRef(), initArgs);
  }

  public JAssignStmt invokeMethod(JimpleLocal refLocal, String methodClass, String methodName, Type returnType, List<Value> args) {
    JimpleLocal resultLocal = WriterUtils.generateLocal(returnType);
    SootClass sootClass = generator.getSootClass(methodClass);
    SootMethod method = sootClass.getMethod(methodName);
    JVirtualInvokeExpr invokeExpr =
        new JVirtualInvokeExpr(refLocal, method.makeRef(), args);

    return new JAssignStmt(resultLocal, invokeExpr);
  }

  public List<Unit> printf(String message, List<Value> args) {
    ArrayList<Unit> statements = new ArrayList<>();

    // Print the random length
    JimpleLocal printLocal = WriterUtils.generateLocal(RefType.v("java.io.PrintStream"));
    SootField sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>");
    statements.add(new JAssignStmt(printLocal, Jimple.v().newStaticFieldRef(sysOutField.makeRef())));

    // Create array for print parameters
    JimpleLocal arrayLocal = WriterUtils.generateLocal(RefType.v("java.lang.Object"));
    statements.add(new JAssignStmt(arrayLocal, IntConstant.v(args.size())));

    // Assign args values to array
    for (int i = 0; i <= args.size(); i++) {
      JArrayRef arrayForPrint = new JArrayRef(arrayLocal, IntConstant.v(i));
      statements.add(new JAssignStmt(arrayForPrint, args.get(i)));
    }

    // Print
    SootClass printStreamClass = Scene.v().getSootClass("java.io.PrintStream");
    SootMethod printStreamMethod = printStreamClass.getMethod("printf");
    StringConstant printMsg = StringConstant.v(message);
    JVirtualInvokeExpr printInvokeExpr =
        new JVirtualInvokeExpr(printLocal, printStreamMethod.makeRef(), List.of(printMsg, arrayLocal));
    statements.add(new JInvokeStmt(printInvokeExpr));

    return statements;
  }

  public JInvokeStmt sleep(JimpleLocal sleepLengthLocal) {
    SootClass threadClass = Scene.v().getSootClass("java.lang.Thread");
    SootMethod sleepMethod = threadClass.getMethod("sleep");
    return new JInvokeStmt(new JStaticInvokeExpr(sleepMethod.makeRef(), List.of(sleepLengthLocal)));
  }
}
