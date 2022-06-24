package oscar.transformers.noisers.shared;

import oscar.controller.noise.NoisePlacement;
import oscar.engine.Engine;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleSceneTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SharedVariableNoiser extends JimpleSceneTransformer {

  public SharedVariableNoiser() {
    super("svn", SharedVariableNoiser.class, SharedVariableNoiser::routine);
  }

  private static void routine(JimpleBodyBox bodyBox) {
    // Check if this method is the main method. No shared accesses should occur here
    if (bodyBox.body().getMethod().getDeclaringClass().getName().equals(Engine.getMainClass())) {
      if (List.of("main", "main_wrapped", "<clinit>").contains(bodyBox.body().getMethod().getName()))
        return;
    }

    HashSet<JimpleLocal> sharedLocals = new HashSet<>();

    // Sequentially process every statement
    for (Unit stmt : bodyBox.body().getUnits()) {
      // Add all parameters to sharedLocals
      if (stmt instanceof JIdentityStmt) {
        sharedLocals.add((JimpleLocal) ((JIdentityStmt) stmt).getLeftOp());
        continue;
      }

      // Process assignments
      if (stmt instanceof JAssignStmt) {
        processAssignment((JAssignStmt) stmt, sharedLocals);
        continue;
      }

      // Add all parameter vars

      // Add all volatile vars

      // TODO if method returns shared var?
      // TODO array of reftypes

      // TODO Check methods that take shared vars as parameter

      // TODO noise all static vars
      // TODO shared method calls
      // Get all references to shared variables

      // Get access to volatile variables

      // Get accesses to variables

    }

    // Noise all shared accesses
    List<Unit> sharedAccesses = bodyBox.body().getUnits().stream()
                                       .filter(u -> u.hasTag(NoiserTag.SHARED_VAR_ACCESS.getName()))
                                       .collect(Collectors.toList());

    for (Unit unit : sharedAccesses)
      if (unit.hasTag(NoiserTag.SHARED_VAR_ACCESS.getName()))
        noiseStmt(unit, bodyBox);
  }

  private static void processAssignment(JAssignStmt stmt, HashSet<JimpleLocal> sharedLocals) {
    // Check if lvalue is a static field ref, tag if so
    if (stmt.getLeftOp() instanceof StaticFieldRef) {
      if (!stmt.hasTag(NoiserTag.SHARED_VAR_ACCESS.getName()))
        stmt.addTag(NoiserTag.SHARED_VAR_ACCESS);
      return;
    }

    // Get lvalues and rvalues
    // Check if lvalue is a jimple local
    if (!(stmt.getLeftOp() instanceof JimpleLocal))
      throw new RuntimeException("Expected a Jimple Local when fetching lvalue.");

    JimpleLocal lValue = (JimpleLocal) stmt.getLeftOp();

    // Get rvalues, ignoring constants
    List<Value> rValues = stmt.getRightOp().getUseBoxes().stream()
                              .map(ValueBox::getValue)
                              .filter(v -> !(v instanceof Constant))
                              .collect(Collectors.toList());

    // Check if any rValue is a static field ref, noise if so
    if (rValues.stream().anyMatch(v -> v instanceof StaticFieldRef)) {
      if (!stmt.hasTag(NoiserTag.SHARED_VAR_ACCESS.getName()))
        stmt.addTag(NoiserTag.SHARED_VAR_ACCESS);
      return;
    }

    // Check if any rvalue in statement is a shared jimple local, noise if so
    for (Value rValue : rValues) {
      if (!(stmt.getLeftOp() instanceof JimpleLocal))
        throw new RuntimeException("Expected a Jimple Local for rvalue.");

      if (sharedLocals.contains((JimpleLocal) rValue)) {
        sharedLocals.add(lValue);
        if (!stmt.hasTag(NoiserTag.SHARED_VAR_ACCESS.getName()))
          stmt.addTag(NoiserTag.SHARED_VAR_ACCESS);
      }

      if (sharedLocals.contains(lValue)) {
        sharedLocals.add((JimpleLocal) rValue);
        if (!stmt.hasTag(NoiserTag.SHARED_VAR_ACCESS.getName()))
          stmt.addTag(NoiserTag.SHARED_VAR_ACCESS);
      }
    }
  }


  /**
   * Add noise statements before and after access to a shared variable
   *
   * @param unit    the unit which has an access to a shared variable
   * @param bodyBox the body box of this method
   */
  private static void noiseStmt(Unit unit, JimpleBodyBox bodyBox) {
    bodyBox.body()
           .getUnits()
           .insertBefore(bodyBox.generator().Statement.noise(NoisePlacement.BEFORE_SHARED_VARIABLE_ACCESS), unit);

    bodyBox.body()
           .getUnits()
           .insertAfter(bodyBox.generator().Statement.noise(NoisePlacement.AFTER_SHARED_VARIABLE_ACCESS), unit);
  }
}
