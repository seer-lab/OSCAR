package oscar.transformers.injectors;

import oscar.transformers.CustomJimpleTransformer;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JInvokeStmt;

import java.util.List;
import java.util.Map;

public class ExitCapture extends CustomJimpleTransformer {
  public ExitCapture() {
    super("jtp", "ec");
  }

  @Override
  protected final void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // Check if class is blacklisted
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Cycle all methods to find calls to system.exit
    for (Unit unit : body.getUnits()) {
      if (!(unit instanceof JInvokeStmt))
        continue;

      InvokeExpr invokeExpr = ((JInvokeStmt) unit).getInvokeExpr();
      SootMethod invokeMethod = invokeExpr.getMethod();

      if (!invokeMethod.getDeclaringClass().getName().equals("java.lang.System"))
        continue;

      if (!invokeMethod.getName().equals("exit"))
        continue;

      SootMethodRefImpl methodRef = new SootMethodRefImpl(
          Scene.v().getSootClass("oscar.controller.Controller"),
          "exit",
          List.of(IntType.v()),
          VoidType.v(),
          true
      );

      invokeExpr.setMethodRef(methodRef);
    }

    body.validate();
  }
}