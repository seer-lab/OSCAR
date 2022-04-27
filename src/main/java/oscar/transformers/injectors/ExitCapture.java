package oscar.transformers.injectors;

import oscar.engine.CustomJimpleBody;
import oscar.engine.Engine;
import oscar.transformers.CustomJimpleTransformer;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JInvokeStmt;

import java.util.List;
import java.util.Map;

public class ExitCapture extends CustomJimpleTransformer {
  public ExitCapture() {
    super("jtp", "ec", ExitCapture.class, ExitCapture::routine);
  }

  private static void routine(CustomJimpleBody body) {
    // Cycle all methods to find calls to system.exit
    for (Unit unit : body.v().getUnits()) {
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
  }
}