package oscar.transformers.analysers;

import java.util.Set;

public class AssignmentVariables {
  private final Variable lValue;
  private final Set<Variable> rValues;

  public AssignmentVariables(Variable lValue, Set<Variable> rValues) {
    this.lValue = lValue;
    this.rValues = rValues;
  }

  public Variable getLValue() {
    return lValue;
  }

  public Set<Variable> getRValues() {
    return rValues;
  }
}
