package oscar.transformers.noisers.shared;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.Engine;
import oscar.engine.body.Metadata;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;

import java.util.List;

public class SharedVariableNoiser extends JimpleTransformer {
  public SharedVariableNoiser() {
    super("jtp", "svn", SharedVariableNoiser.class, SharedVariableNoiser::routine);
  }

  /**
   * This transformer will do a depth-first search through the bodies
   * of all the thread run methods and, recursively, the methods they call
   *
   * @param bodyBox - The body of a jimple method
   */
  private static void routine(JimpleBodyBox bodyBox) {
    // Check if method belongs to a runnable class
    if (!bodyBox.body().getMethod().getDeclaringClass().implementsInterface("java.lang.Runnable") || true)
      return;

    // Check if current body belongs to thread run method
    if (!"run".equals(bodyBox.body().getMethod().getName()))
      return;

    // Recursively check for shared variables
    processBody(bodyBox.body());
  }

  public static void processBody(JimpleBody b) {
    // Check if method belongs to blacklisted class
    if (Engine.isClassBlacklisted(b.getMethod()))
      return;

    // Check if body has already been noised
    if (b.hasTag(NoiserTag.BODY_SHARED_VARIABLES_NOISED.getStringValue()))
      return;

    JimpleBodyBox bodyBox = new JimpleBodyBox(b);

    // Check body statements serially
    for (Unit stmt : bodyBox.body().getUnits()) {
      // Process assignments
      if (stmt instanceof JAssignStmt)
        processAssignment(bodyBox, (JAssignStmt) stmt);

      // Process invocations
      if (stmt instanceof JInvokeStmt)
        processInvoke(bodyBox, (JInvokeStmt) stmt);
    }

    b.addTag(NoiserTag.BODY_SHARED_VARIABLES_NOISED);

    b.validate();
  }

  private static void processInvoke(JimpleBodyBox bodyBox, JInvokeStmt stmt) {
    // Check type of invoke
    if (stmt.getInvokeExpr() instanceof JVirtualInvokeExpr) {
      // Get and validate variable from which static invocation is made
      List<ValueBox> boxes = stmt.getInvokeExprBox().getValue().getUseBoxes();

      if (boxes.size() != 1)
        throw new RuntimeException("Unexpected box size.");

      if (!(boxes.get(0).getValue() instanceof JimpleLocal))
        throw new RuntimeException("Unexpected box type.");

      JimpleLocal boxLocal = (JimpleLocal) boxes.get(0).getValue();

      // Check if object that invokes method is shared and process the body if so
      if (bodyBox.hasMetadata(Metadata.SHARED_VARIABLES, boxLocal.getName()))
        processBody((JimpleBody) stmt.getInvokeExpr().getMethod().getActiveBody());
    } else if (stmt.getInvokeExpr() instanceof JStaticInvokeExpr) {
      // We can consider all static properties shared. As such we will noise the method
      processBody((JimpleBody) stmt.getInvokeExpr().getMethod().getActiveBody());
    } else
      throw new RuntimeException("Unsupported type of expression '" + stmt.getInvokeExpr().getClass().getName() + "'");
  }

  private static void processAssignment(JimpleBodyBox bodyBox, JAssignStmt stmt) {
    // Check if we need to dismantle this statement
    // First we check if left value is a shared variable local
    if (!stmt.hasTag(NoiserTag.DISMANTLED_ASSIGNMENT.getName())) {
      if (bodyBox.hasMetadata(Metadata.SHARED_VARIABLES, ((JimpleLocal) stmt.getLeftOp()).getName())) {
        // If so, dismantle assign statement and process them
        List<JAssignStmt> newStatements = dismantleAssignment(bodyBox, stmt);
        // Process first statement again (c = b)
        processAssignment(bodyBox, newStatements.get(0));

        // Noise second statement (a = c)
        noiseStatement(bodyBox, newStatements.get(1));
      }
    }

    // Check if right op is an access to an instance field
    if (stmt.containsFieldRef()) {
      // Check if the field is static
      if (stmt.getFieldRef().getField().isStatic()) {
        // Noise access
        noiseStatement(bodyBox, stmt);

        // Signal local as shared
        bodyBox.addMetadata(Metadata.SHARED_VARIABLES, ((JimpleLocal) stmt.getLeftOp()).getName());
      }
    }

    // Check if assignment is an invoke expression
    if (stmt.containsInvokeExpr()) {

    }

    System.out.println();
  }

  private static void noiseStatement(JimpleBodyBox bodyBox, Unit unit) {
    bodyBox.body()
           .getUnits()
           .insertBefore(bodyBox.generator().Statement.sleep(NoisePlacement.BEFORE_SHARED_VARIABLE_ACCESS), unit);
    bodyBox.body()
           .getUnits()
           .insertBefore(bodyBox.generator().Statement.signal(NoiseCategory.SHARED_VARIABLE_BASED), unit);
    bodyBox.body()
           .getUnits()
           .insertAfter(bodyBox.generator().Statement.sleep(NoisePlacement.AFTER_SHARED_VARIABLE_ACCESS), unit);

    System.out.println();
  }

  private static List<JAssignStmt> dismantleAssignment(JimpleBodyBox bodyBox, JAssignStmt stmt) {
    // We need to deconstruct the operation from a = b to c = b and a = c
    JimpleLocal newLocal = bodyBox.generator().Local.fromType(stmt.getRightOp().getType());

    // Insert statements and delete original
    JAssignStmt newStmtA = new JAssignStmt(newLocal, stmt.getRightOp());
    JAssignStmt newStmtB = new JAssignStmt(stmt.getLeftOp(), newLocal);

    // Mark both as dismantled assignments, preventing further dismantling
    newStmtA.addTag(NoiserTag.DISMANTLED_ASSIGNMENT);
    newStmtB.addTag(NoiserTag.DISMANTLED_ASSIGNMENT);

    bodyBox.body().getUnits().insertBefore(newStmtA, stmt);
    bodyBox.body().getUnits().insertBefore(newStmtB, stmt);

    bodyBox.body().getUnits().remove(stmt);

    return List.of(newStmtA, newStmtB);
  }
}
