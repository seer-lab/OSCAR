package oscar.engine.generators;

import soot.*;
import soot.javaToJimple.DefaultLocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JimpleLocal;

import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class JimpleGenerator {
  private final JimpleBody body;
  private final DefaultLocalGenerator localGenerator;

  public final StatementGenerator Statement;
  public final ConversionGenerator Conversion;
  public final ArithmeticGenerator Arithmetic;

  public JimpleGenerator(JimpleBody body) {
    this.body = body;
    this.localGenerator = new DefaultLocalGenerator(body);

    this.Statement = new StatementGenerator(this);
    this.Conversion = new ConversionGenerator(this);
    this.Arithmetic = new ArithmeticGenerator(this);
  }

  public JimpleLocal getLocal(Type type) {
    return (JimpleLocal) localGenerator.generateLocal(type);
  }

  public JimpleLocal generateLocalFromClass(String className) {
    SootClass sootClass = Scene.v().getSootClass(className);
    return getLocal(RefType.v(sootClass));
  }

  public JimpleLocal generateLocalFromClass(SootClass sootClass) {
    return getLocal(RefType.v(sootClass));
  }

  public void addExceptionToBodyMethod(String exceptionClass) {
    SootClass interruptedException = Scene.v().getSootClass(exceptionClass);
    body.getMethod().addExceptionIfAbsent(interruptedException);
  }

  public SootClass getSootClass(String className) {
    return Scene.v().getSootClass(className);
  }

  public void appendUnit(Unit unit) {
    Unit indexUnit = body.getUnits().stream()
                                  .filter(Predicate.not(JIdentityStmt.class::isInstance))
                                  .findFirst().get();

    body.getUnits().insertBefore(unit, indexUnit);
  }

  public JAssignStmt array(String arrayTypeName, int arrayDimensions, int arraySize) {
    JimpleLocal arrayLocal = getLocal(ArrayType.v(RefType.v(arrayTypeName), arrayDimensions));
    return new JAssignStmt(arrayLocal, new JNewArrayExpr(RefType.v(arrayTypeName), IntConstant.v(arraySize)));
  }
}
