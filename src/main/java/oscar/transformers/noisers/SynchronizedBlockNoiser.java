package oscar.transformers.noisers;

import oscar.controllers.noise.NoisePlacement;
import oscar.engine.Engine;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SynchronizedBlockNoiser extends CustomJimpleTransformer {
  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (!isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Find invocations of synchronized methods
    List<JInvokeStmt> syncMethodInvocations =
        body.getUnits().stream()
                 .filter(JInvokeStmt.class::isInstance)
                 .map(box -> ((JInvokeStmt) box))
                 .filter(box -> box.getInvokeExpr().getMethod().isSynchronized())
                 .filter(box -> !box.getInvokeExpr().getMethod().getDeclaringClass().getName().equals("java.lang.Thread"))
                 .collect(Collectors.toList());

    // Nothing to change, leave
    if (syncMethodInvocations.isEmpty())
      return;


    for (JInvokeStmt syncMethodInvocation : syncMethodInvocations) {
      // Create statement to insert sleep noise before and after statement
      generateNoiseStatement(body, syncMethodInvocation, NoisePlacement.SYNC_BASED_BEFORE_SYNC_BLOCK);
      generateNoiseStatement(body, syncMethodInvocation, NoisePlacement.SYNC_BASED_AFTER_SYNC_BLOCK);
    }

    body.validate();
  }

  private void generateNoiseStatement(Body body, Unit location, NoisePlacement noisePlacement) {
    // Initialize jimple generator
    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    // Create statement to insert sleep noise with a random id
    long noiseLocationID = Engine.generateLocationID();
    JimpleLocal idLocal = generator.getLocal(LongType.v());

    String noisePlacementShorthand = noisePlacement.getShorthand();

    JAssignStmt idAssignStmt = new JAssignStmt(idLocal, LongConstant.v(noiseLocationID));
    Stmt noiseStmt = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void sleep(long,java.lang.String)", List.of(idLocal, StringConstant.v(noisePlacementShorthand)));

    if (noisePlacement == NoisePlacement.SYNC_BASED_BEFORE_SYNC_BLOCK) {
      body.getUnits().insertBefore(idAssignStmt, location);
      body.getUnits().insertBefore(noiseStmt, location);
    } else if (noisePlacement == NoisePlacement.SYNC_BASED_AFTER_SYNC_BLOCK) {
      body.getUnits().insertAfter(noiseStmt, location);
      body.getUnits().insertAfter(idAssignStmt, location);
    } else
      throw new RuntimeException("Invalid noise placement type");
  }
}
