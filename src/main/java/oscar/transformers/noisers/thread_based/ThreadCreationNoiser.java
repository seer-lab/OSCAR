package oscar.transformers.noisers.thread_based;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.Body;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEnterMonitorStmt;
import soot.jimple.internal.JExitMonitorStmt;
import soot.jimple.internal.JInvokeStmt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ThreadCreationNoiser extends CustomJimpleTransformer {
  public ThreadCreationNoiser() {
    super("jtp", "tcn");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    if (!body.getMethod().getDeclaringClass().hasTag(NoiserTag.THREAD_LAUNCHED.getName()))
      return;

    if (!body.getMethod().getName().equals("run"))
      return;

    /*// Find monitor calls for synchronized blocks
    List<Unit> threadAssignStmts = getThreadLaunchStatements((JimpleBody) body);
    List<Unit> threadFunctions = getThreadLaunchStatements((JimpleBody) body);

    // No statements with sync method calls or blocks found, leave
    if (threadLaunches.isEmpty())
      return;

    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    // Create statement to insert sleep noise before and after sync blocks
    for (Unit monitorCall : threadLaunches) {

      body.getUnits().insertBefore(generator.Statement.sleep(NoisePlacement.BEFORE_SYNC_BLOCK), monitorCall);

      body.getUnits().insertBefore(generator.Statement.signal(NoiseCategory.SYNCHRONIZATION_BASED), monitorCall);
      List<Unit> units = generator.Statement.sleep(NoisePlacement.AFTER_SYNC_BLOCK);
      body.getUnits().insertAfter(units, monitorCall);

    }
  */
    body.validate();
  }

  private static List<JAssignStmt> getThreadAssignStmts(JimpleBody body) {
    List<JAssignStmt> assignStmts = body.getUnits()
                                        .stream()
                                        .filter(s -> s instanceof JAssignStmt)
                                        .map(s -> ((JAssignStmt) s))
                                        .collect(Collectors.toList());

    // Check assign statements with taht use
    return null;
  }


  private static List<Unit> getThreadFunctions(JimpleBody body, List<JInvokeStmt> stmts) {
    return stmts.stream()
                .map(body.getUnits()::getPredOf)
                .filter(s -> s instanceof JInvokeStmt)
                .peek(s -> System.out.println(((JInvokeStmt) s).getInvokeExpr().getMethod().getSignature()))
                .filter(s -> List.of("<java.lang.Thread: void start()>", "<java.lang.Thread: void run()>")
                                 .contains(((JInvokeStmt) s).getInvokeExpr().getMethod().getSignature())
                )
                .collect(Collectors.toList());
  }
}
