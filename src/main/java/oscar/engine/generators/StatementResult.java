package oscar.engine.generators;

import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;

public record StatementResult(JimpleLocal local, Stmt stmt) {}
