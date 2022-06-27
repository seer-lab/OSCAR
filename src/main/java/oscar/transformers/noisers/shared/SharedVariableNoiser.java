package oscar.transformers.noisers.shared;

import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleSceneTransformer;
import oscar.transformers.analysers.AssignmentVariables;
import oscar.transformers.analysers.Variable;
import oscar.transformers.analysers.VariableType;
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
      AssignmentVariables assignmentVariables = Variable.getVariablesFromAssignment(assignment, bodyBox);

      Variable lValueVar = assignmentVariables.getLValue();
      Set<Variable> rValueVars = assignmentVariables.getRValues();

      // Ignore if lValue is a local
      if (lValueVar.getType() == VariableType.LOCAL)
        continue;

      // Check if there is a dependency clash between lValue and rValues
      boolean dependencyClash = false;

      for (Variable rValueVar : rValueVars) {
        HashSet<String> lValueDependencies = variableDependencies.get(lValueVar.getName());
        HashSet<String> rValueDependencies = variableDependencies.get(rValueVar.getName());

        if (lValueDependencies == null || rValueDependencies == null)
          continue;

        lValueDependencies.retainAll(rValueDependencies);

        // Dependency clash found, break and exit loop
        if (!lValueDependencies.isEmpty()) {
          dependencyClash = true;
          break;
        }
      }

      // Noise this statement, if a dependency clash was found
      if (dependencyClash) {
        bodyBox.body()
               .getUnits()
               .insertBefore(bodyBox.generator().Statement.noise(NoisePlacement.BEFORE_SHARED_VARIABLE_ACCESS), assignment);

        bodyBox.body()
               .getUnits()
               .insertAfter(bodyBox.generator().Statement.noise(NoisePlacement.AFTER_SHARED_VARIABLE_ACCESS), assignment);

      }
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
}
