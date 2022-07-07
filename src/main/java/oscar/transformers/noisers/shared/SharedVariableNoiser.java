package oscar.transformers.noisers.shared;

import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleSceneTransformer;
import oscar.transformers.analysers.StatementVariables;
import oscar.transformers.analysers.Variable;
import soot.jimple.internal.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SharedVariableNoiser extends JimpleSceneTransformer {

  private final HashMap<String, HashSet<String>> variableDependencies;

  public SharedVariableNoiser(HashMap<String, HashSet<String>> variableDependencies) {
    super("svn", SharedVariableNoiser.class);
    this.routine = this::routine;
    this.variableDependencies = variableDependencies;

  }

  private void routine(JimpleBodyBox bodyBox) {
    // Get all assignments
    List<JAssignStmt> assignments = bodyBox.body()
                                           .getUnits()
                                           .stream()
                                           .filter(JAssignStmt.class::isInstance)
                                           .map(JAssignStmt.class::cast)
                                           .collect(Collectors.toList());

    // Sequentially process every assign statement
    for (JAssignStmt assignment : assignments) {
      // Get lvalue and rvalues
      StatementVariables statementVariables = Variable.getVariablesFromAssignment(assignment, bodyBox);

      Variable lValueVar = statementVariables.getLValue();
      Set<Variable> rValueVars = statementVariables.getRValues();

      // Check if there is a dependency clash between lValue and rValues
      boolean dependencyClash = false;

      for (Variable rValueVar : rValueVars) {
        HashSet<String> lValueDependencies = variableDependencies.get(lValueVar.getName());
        HashSet<String> rValueDependencies = variableDependencies.get(rValueVar.getName());

        if (lValueDependencies == null || rValueDependencies == null)
          continue;

        // Dependency clash found, break and exit loop
        if (lValueDependencies.stream().anyMatch(rValueDependencies::contains)) {
          dependencyClash = true;
          break;
        }
      }

      // Noise this assignment, if a dependency clash was found
      if (!dependencyClash)
        continue;

      // Make sure it is not a return statement
      if (lValueVar.isReturn() || rValueVars.stream().anyMatch(Variable::isReturn))
        continue;

      // Check noise placement type
      NoisePlacement beforeNoisePlacement;
      NoisePlacement afterNoisePlacement;

      if (lValueVar.isField() || rValueVars.stream().anyMatch(Variable::isField)) {
        beforeNoisePlacement = NoisePlacement.BEFORE_SHARED_FIELD_ACCESS;
        afterNoisePlacement = NoisePlacement.AFTER_SHARED_FIELD_ACCESS;
      } else {
        beforeNoisePlacement = NoisePlacement.BEFORE_SHARED_LOCAL_ACCESS;
        afterNoisePlacement = NoisePlacement.AFTER_SHARED_LOCAL_ACCESS;
      }

      bodyBox.body()
             .getUnits()
             .insertBefore(bodyBox.generator().Statement.noise(beforeNoisePlacement), assignment);

      bodyBox.body()
             .getUnits()
             .insertAfter(bodyBox.generator().Statement.noise(afterNoisePlacement), assignment);
    }

  }
}
