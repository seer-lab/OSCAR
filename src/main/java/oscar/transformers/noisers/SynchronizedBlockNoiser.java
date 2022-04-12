package oscar.transformers.noisers;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SynchronizedBlockNoiser extends CustomJimpleTransformer {
  public SynchronizedBlockNoiser() {
    super("jtp", "sbn");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Find monitor calls for synchronized blocks
    List<Unit> monitorCalls = getMonitorCalls((JimpleBody) body);

    // No statements with sync method calls or blocks found, leave
    if (monitorCalls.isEmpty())
      return;

    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    // Create statement to insert sleep noise before and after sync blocks
    for (Unit monitorCall : monitorCalls) {
      if (monitorCall instanceof JEnterMonitorStmt) {
        body.getUnits().insertBefore(generator.Statement.sleep(NoisePlacement.BEFORE_SYNC_BLOCK), monitorCall);
        body.getUnits().insertBefore(generator.Statement.signal(NoiseCategory.SYNCHRONIZATION_BASED), monitorCall);
      } else if (monitorCall instanceof JExitMonitorStmt) {
        List<Unit> units = generator.Statement.sleep(NoisePlacement.AFTER_SYNC_BLOCK);
        body.getUnits().insertAfter(units, monitorCall);
      } else
        throw new RuntimeException("Invalid monitor call statement");
    }

    body.validate();
  }

  private static List<Unit> getMonitorCalls(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(s -> s instanceof JEnterMonitorStmt || s instanceof JExitMonitorStmt)
               .collect(Collectors.toList());
  }
}
