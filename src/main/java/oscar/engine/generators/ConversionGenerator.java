package oscar.engine.generators;

import oscar.engine.WriterUtils;
import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JimpleLocal;

import java.util.List;

public record ConversionGenerator(JimpleGenerator generator) {
  public JAssignStmt valueOf(Type originType, String originTypeClass, Value valueLocal) {
    JimpleLocal local = WriterUtils.generateLocal(originType);
    SootClass sootClass = Scene.v().getSootClass(originTypeClass);
    SootMethod longValueOf = sootClass.getMethod("valueOf");
    JStaticInvokeExpr valueOfLong = new JStaticInvokeExpr(longValueOf.makeRef(), List.of(valueLocal));
    return new JAssignStmt(local, valueOfLong);
  }
}
