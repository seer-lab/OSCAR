package oscar.transformers.noisers.sync_based;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.Engine;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SynchronizedMethodCallNoiser extends CustomJimpleTransformer {
  public SynchronizedMethodCallNoiser() {
    super("jtp", "smcn");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    Engine.startTransformer(this.getClass(), body);

    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Find invocations of synchronized methods
    List<JInvokeStmt> syncMethodInvocations = getSyncMethodInvocations((JimpleBody) body);

    // No statements with sync method calls or blocks found, leave
    if (syncMethodInvocations.isEmpty())
      return;

    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    // Create statement to insert sleep noise before and after sync blocks
    for (Unit invocation : syncMethodInvocations) {
      body.getUnits().insertBefore(generator.Statement.sleep(NoisePlacement.BEFORE_SYNC_BLOCK), invocation);
      body.getUnits().insertBefore(generator.Statement.signal(NoiseCategory.SYNCHRONIZATION_BASED), invocation);

      body.getUnits().insertAfter(generator.Statement.sleep(NoisePlacement.AFTER_SYNC_BLOCK), invocation);
    }

    body.validate();

    Engine.endTransformer(this.getClass(), body);
  }

  private static List<JInvokeStmt> getSyncMethodInvocations(JimpleBody body) {
    return body.getUnits().stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(box -> ((JInvokeStmt) box))
               .filter(box -> box.getInvokeExpr().getMethod().isSynchronized())
               .filter(box -> !box.getInvokeExpr().getMethod().getDeclaringClass().getName().equals("java.lang.Thread"))
               .collect(Collectors.toList());
  }
}
