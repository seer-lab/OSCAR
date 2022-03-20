package oscar.engine.writers;

import soot.*;
import soot.jimple.*;

import java.util.List;

public class PrintWriter {
  public static List<Stmt> createStatement(Body body, String content) {
    Local psLocal = WriterUtils.generateNewLocal(body, RefType.v("java.io.PrintStream"));

    // Now we assign "System.out" to psLocal
    SootField sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>");
    AssignStmt sysOutAssignStmt = Jimple.v().newAssignStmt(psLocal, Jimple.v().newStaticFieldRef(sysOutField.makeRef()));

    // Create println method call and provide its parameter
    SootMethod printlnMethod = Scene.v().grabMethod("<java.io.PrintStream: void println(java.lang.String)>");
    Value printlnParamter = StringConstant.v(content);
    InvokeStmt printlnMethodCallStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, printlnMethod.makeRef(), printlnParamter));

    return List.of(sysOutAssignStmt, printlnMethodCallStmt);
  }
}

