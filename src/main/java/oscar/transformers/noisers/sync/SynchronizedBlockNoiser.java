package oscar.transformers.noisers.sync;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleTransformer;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.List;
import java.util.stream.Collectors;

public final class SynchronizedBlockNoiser extends JimpleTransformer {

  public SynchronizedBlockNoiser() {
    super( "sbn", SynchronizedBlockNoiser.class, SynchronizedBlockNoiser::routine);
  }

  private static void routine(JimpleBodyBox body) {
    // Find monitor calls for synchronized blocks
    List<Unit> monitorCalls = getMonitorCalls(body.body());

    // Create statement to insert sleep noise before and after sync blocks
    for (Unit monitorCall : monitorCalls) {
      if (monitorCall instanceof JEnterMonitorStmt) {
        body.body().getUnits().insertBefore(body.generator().Statement.noise(NoisePlacement.BEFORE_SYNC_BLOCK), monitorCall);
        body.body().getUnits().insertBefore(body.generator().Statement.signal(NoiseCategory.SYNCHRONIZATION_BASED), monitorCall);
      } else if (monitorCall instanceof JExitMonitorStmt) {
        List<Unit> units = body.generator().Statement.noise(NoisePlacement.AFTER_SYNC_BLOCK);
        body.body().getUnits().insertAfter(units, monitorCall);
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
