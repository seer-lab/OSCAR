package oscar.transformers.noisers.lock;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleTransformer;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;

import java.util.List;
import java.util.stream.Collectors;

public final class ReentrantLockNoiser extends JimpleTransformer {

  public ReentrantLockNoiser() {
    super("rln", ReentrantLockNoiser.class, ReentrantLockNoiser::routine);
  }

  private static void routine(JimpleBodyBox body) {
    // Find calls to reentrant lock locks and unlocks
    List<JInvokeStmt> reentrantLockCalls = getReentrantLockCalls(body.body());

    // Create statement to insert sleep noise before and after sync blocks
    for (JInvokeStmt lockCall : reentrantLockCalls) {
      if (getInvokeExprMethodName(lockCall).equals("lock")) {
        body.body().getUnits().insertBefore(body.generator().Statement.sleep(NoisePlacement.BEFORE_REENTRANT_LOCK_LOCK), lockCall);
        body.body().getUnits().insertBefore(body.generator().Statement.signal(NoiseCategory.LOCK_BASED), lockCall);
      } else if (getInvokeExprMethodName(lockCall).equals("unlock")) {
        List<Unit> units = body.generator().Statement.sleep(NoisePlacement.AFTER_REENTRANT_LOCK_UNLOCK);
        body.body().getUnits().insertAfter(units, lockCall);
      } else
        throw new RuntimeException("Invalid reentrant lock call statement");
    }
  }

  private static List<JInvokeStmt> getReentrantLockCalls(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(JInvokeStmt.class::cast)
               .filter(s -> s.getInvokeExpr() instanceof JVirtualInvokeExpr)
               .filter(s -> getInvokeExprClassName(s).equals("java.util.concurrent.locks.ReentrantLock"))
               .filter(s -> List.of("lock", "unlock").contains(getInvokeExprMethodName(s)))
               .collect(Collectors.toList());
  }

  private static String getInvokeExprMethodName(JInvokeStmt expr) {
    return expr.getInvokeExpr().getMethodRef().getName();
  }

  private static String getInvokeExprClassName(JInvokeStmt expr) {
    return expr.getInvokeExpr().getMethodRef().getDeclaringClass().getName();
  }
}
