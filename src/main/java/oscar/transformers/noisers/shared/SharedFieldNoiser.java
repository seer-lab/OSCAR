package oscar.transformers.noisers.shared;

import oscar.engine.CustomJimpleBody;
import oscar.transformers.CustomJimpleTransformer;
import soot.UnitBox;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JAssignStmt;

import java.util.List;

public class SharedFieldNoiser extends CustomJimpleTransformer {
  private static

  public SharedFieldNoiser() {
    super("jtp", "sfn", SharedFieldNoiser.class, SharedFieldNoiser::routine);
  }

  private static void routine(CustomJimpleBody body) {
    // Check if current body belongs to run method
    if (!body.v().getMethod().getName().equals("run"))
      return;

    recursiveNoiser(body.v());
  }

  public static void recursiveNoiser(JimpleBody b) {
    CustomJimpleBody body = new CustomJimpleBody(b);

    // Process all variable assignments if they involve shared variables
    body.v().getUnits().stream()
        .filter(JAssignStmt.class::isInstance)
        .map(JAssignStmt.class::cast)
        .filter(s -> s.getUnitBoxes().size() == 2)
        .filter(s -> s.getUnitBoxes().get(1).getUnit(). instanceof )
        .filter(s -> body.v().getLocals().contains(s.getUnitBoxes()))
        .forEach(SharedFieldNoiser::processAssignments);

  }

  private static void processAssignments(JAssignStmt stmt) {

  }
}
