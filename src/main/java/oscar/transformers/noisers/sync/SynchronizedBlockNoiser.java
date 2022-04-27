package oscar.transformers.noisers.sync;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.CustomJimpleBody;
import oscar.transformers.CustomJimpleTransformer;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.List;
import java.util.stream.Collectors;

public final class SynchronizedBlockNoiser extends CustomJimpleTransformer {

  public SynchronizedBlockNoiser() {
    super("jtp", "sbn", SynchronizedBlockNoiser.class, SynchronizedBlockNoiser::routine);
  }

  private static void routine(CustomJimpleBody body) {
    // Find monitor calls for synchronized blocks
    List<Unit> monitorCalls = getMonitorCalls(body.v());

    // Create statement to insert sleep noise before and after sync blocks
    for (Unit monitorCall : monitorCalls) {
      if (monitorCall instanceof JEnterMonitorStmt) {
        body.v().getUnits().insertBefore(body.g().Statement.sleep(NoisePlacement.BEFORE_SYNC_BLOCK), monitorCall);
        body.v().getUnits().insertBefore(body.g().Statement.signal(NoiseCategory.SYNCHRONIZATION_BASED), monitorCall);
      } else if (monitorCall instanceof JExitMonitorStmt) {
        List<Unit> units = body.g().Statement.sleep(NoisePlacement.AFTER_SYNC_BLOCK);
        body.v().getUnits().insertAfter(units, monitorCall);
      } else
        throw new RuntimeException("Invalid monitor call statement");
    }
  }

  private static List<Unit> getMonitorCalls(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(s -> s instanceof JEnterMonitorStmt || s instanceof JExitMonitorStmt)
               .collect(Collectors.toList());
  }
}
