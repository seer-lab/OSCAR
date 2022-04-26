package oscar.transformers.noisers.thread_based;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.Engine;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.Body;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ThreadCreationNoiser extends CustomJimpleTransformer {
  public ThreadCreationNoiser() {
    super("jtp", "tcn");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    Engine.startTransformer(this.getClass(), body);

    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Check if class is tagged and current body belongs to run method
    if (!body.getMethod().getDeclaringClass().hasTag(NoiserTag.THREAD_LAUNCHED.getName()))
      return;

    if (!body.getMethod().getName().equals("run"))
      return;

    // Insert noise statement after first (identity statement)
    Unit identityStmt = body.getUnits().getFirst();

    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);
    body.getUnits().insertAfter(generator.Statement.sleep(NoisePlacement.BEFORE_THREAD_ROUTINE), identityStmt);

    body.validate();

    Engine.endTransformer(this.getClass(), body);
  }
}
