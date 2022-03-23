package oscar.engine.generators;

import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JimpleLocal;

import java.util.List;

public record ConversionGenerator(JimpleGenerator generator) {
  private JAssignStmt valueOf(String originTypeClass, String valueOfMethodSig, Value valueLocal) {
    JimpleLocal local = generator.getLocal(RefType.v(originTypeClass));
    SootClass sootClass = Scene.v().getSootClass(originTypeClass);

    SootMethod longValueOf = sootClass.getMethod(valueOfMethodSig);
    JStaticInvokeExpr valueOfLong = new JStaticInvokeExpr(longValueOf.makeRef(), List.of(valueLocal));

    return new JAssignStmt(local, valueOfLong);
  }

  public JAssignStmt longValueOf(Value valueLocal) {
    return valueOf("java.lang.Long", "java.lang.Long valueOf(long)", valueLocal);
  }
}
