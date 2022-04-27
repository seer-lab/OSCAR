package oscar.transformers.noisers.thread;

import oscar.controller.noise.NoisePlacement;
import oscar.engine.CustomJimpleBody;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.Unit;

public final class ThreadCreationNoiser extends CustomJimpleTransformer {
  public ThreadCreationNoiser() {
    super("jtp", "tcn", ThreadCreationTagger.class, ThreadCreationNoiser::routine);
  }

  private static void routine(CustomJimpleBody body) {
    // Check if class is tagged and current body belongs to run method
    if (body.v().getMethod().getDeclaringClass().hasTag(NoiserTag.THREAD_LAUNCHED.getName()))
      return;

    if (!body.v().getMethod().getName().equals("run"))
      return;

    // Insert noise statement after first (identity statement)
    Unit identityStmt = body.v().getUnits().getFirst();
    body.v().getUnits().insertAfter(body.g().Statement.sleep(NoisePlacement.BEFORE_THREAD_ROUTINE), identityStmt);
  }
}
