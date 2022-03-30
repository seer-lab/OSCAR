package oscar.engine.generators;

import soot.Value;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JRemExpr;

public class ArithmeticGenerator {
  private final JimpleGenerator generator;

  public ArithmeticGenerator(JimpleGenerator generator) {
    this.generator = generator;
  }

  public JAssignStmt modulo(Value valueA, Value valueB) {
    Value result = generator.getLocal(valueA.getType());

    return new JAssignStmt(result, new JRemExpr(valueA, valueB));
  }
}
