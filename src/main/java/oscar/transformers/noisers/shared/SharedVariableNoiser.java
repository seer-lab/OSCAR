package oscar.transformers.noisers.shared;

import oscar.engine.CustomJimpleBody;
import oscar.transformers.JimpleTransformer;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JimpleLocal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SharedVariableNoiser extends JimpleTransformer {
  public SharedVariableNoiser() {
    super("jtp", "svn", SharedVariableNoiser.class, SharedVariableNoiser::routine);
  }

  private static void routine(CustomJimpleBody body) {
    // Check if method belongs to a runnable class
    if (!body.v().getMethod().getDeclaringClass().implementsInterface("java.lang.Runnable"))
      return;

    // Check if current body belongs to class initialization or run method
    // init -> instance, clinit -> static
    if (!List.of("<clinit>", "<init>", "run").contains(body.v().getMethod().getName()))
      return;

    // Recursively check for shared variables
    recursiveNoiser(body.v());
  }

  public static void recursiveNoiser(JimpleBody b) {
    CustomJimpleBody body = new CustomJimpleBody(b);

    // Set<String> parameterInitLocals = body.v().get


    // Get all of this body's parameter variables (which, if used, will be shared)
    Set<String> parameterLocals = body.v().getUnits().stream()
        .filter(JIdentityStmt.class::isInstance)
        .map(JIdentityStmt.class::cast)
        .map(JIdentityStmt::getLeftOpBox)
        .map(ValueBox::getValue)
        .filter(JimpleLocal.class::isInstance)
        .map(JimpleLocal.class::cast)
        .map(JimpleLocal::getName)
        .collect(Collectors.toSet());

    // Process all variable assignments if they involve shared variables
    body.v().getUnits().stream()
        .filter(JAssignStmt.class::isInstance)
        .map(JAssignStmt.class::cast)
        .filter(s -> s.getLeftOpBox().getValue() instanceof JimpleLocal)
        .filter(s -> parameterLocals.contains(((JimpleLocal) s.getLeftOpBox().getValue()).getName()))
        .forEach(SharedVariableNoiser::processAssignments);

    b.validate();
  }

  private static void processAssignments(JAssignStmt stmt) {
    System.out.println();
  }
}
