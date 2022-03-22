package oscar.engine.generators;

import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JimpleLocal;

import java.util.Stack;

public final class JimpleGenerator {
  private int localCounter = 0;
  private final JimpleBody body;
  public StatementGenerator Statement;
  public ConversionGenerator Conversion;
  Stack<Unit> patchUnits;

  public JimpleGenerator(JimpleBody body) {
    this.body = body;
    this.patchUnits = new Stack<>();

    this.Statement = new StatementGenerator(this);
    this.Conversion = new ConversionGenerator(this);
  }

  public JimpleLocal generateLocal(Type type) {
    return new JimpleLocal("__oscar__local__%d".formatted(localCounter++), type);
  }

  public JimpleLocal generateLocalFromClass(String className) {
    SootClass sootClass = Scene.v().getSootClass(className);
    return generateLocal(RefType.v(sootClass));
  }

  public JimpleLocal generateLocalFromClass(SootClass sootClass) {
    return generateLocal(RefType.v(sootClass));
  }

  public void addExceptionToBodyMethod(String exceptionClass) {
    SootClass interruptedException = Scene.v().getSootClass(exceptionClass);
    body.getMethod().addExceptionIfAbsent(interruptedException);
  }

  public SootClass getSootClass(String className) {
    return Scene.v().getSootClass(className);
  }

  public void appendUnit(Unit unit) {
    UnitPatchingChain chain = body.getUnits();
    chain.addFirst(unit);
  }
}
