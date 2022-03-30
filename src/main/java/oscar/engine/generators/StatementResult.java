package oscar.engine.generators;

import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;

public class StatementResult {
  private final Stmt stmt;
  private final JimpleLocal local;

  public StatementResult(JimpleLocal local, Stmt stmt) {
    this.local = local;
    this.stmt = stmt;
  }

  public Stmt getStmt() {
    return stmt;
  }

  public JimpleLocal getLocal() {
    return local;
  }
}
