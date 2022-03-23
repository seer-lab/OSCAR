package oscar.engine.generators;

import soot.Value;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JRemExpr;

public record ArithmeticGenerator(JimpleGenerator generator) {
  public JAssignStmt modulo(Value valueA, Value valueB) {
    Value result = generator.getLocal(valueA.getType());

    return new JAssignStmt(result, new JRemExpr(valueA, valueB));
  }
}
