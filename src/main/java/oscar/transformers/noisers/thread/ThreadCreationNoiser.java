package oscar.transformers.noisers.thread;

import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.Unit;

public final class ThreadCreationNoiser extends JimpleTransformer {
  public ThreadCreationNoiser() {
    super("jtp", "tcn", ThreadCreationTagger.class, ThreadCreationNoiser::routine);
  }

  private static void routine(JimpleBodyBox body) {
    // Check if class is tagged and current body belongs to run method
    if (body.body().getMethod().getDeclaringClass().hasTag(NoiserTag.THREAD_LAUNCHED.getName()))
      return;

    if (!body.body().getMethod().getName().equals("run"))
      return;

    // Insert noise statement after first (identity statement)
    Unit identityStmt = body.body().getUnits().getFirst();
    body.body().getUnits().insertAfter(body.generator().Statement.sleep(NoisePlacement.BEFORE_THREAD_ROUTINE), identityStmt);
  }
}
