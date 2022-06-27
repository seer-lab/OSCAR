package oscar.transformers.analysers;

import oscar.engine.body.JimpleBodyBox;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.*;

import java.util.HashSet;
import java.util.Set;

public class Variable {
  private final VariableType type;
  private final String name;

  public Variable(String name, VariableType type) {
    this.name = name;
    this.type = type;
  }

  public VariableType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Variable))
      return false;

    return this.name.equals(((Variable) obj).getName());
  }

  @Override
  public int hashCode() {
    return this.getName().hashCode();
  }

  public static AssignmentVariables getVariablesFromAssignment(JAssignStmt stmt, JimpleBodyBox bodyBox) {
    String methodName = bodyBox.body().getMethod().getName();

    Variable lValue = getVariable(stmt.getLeftOp(), methodName);
    Set<Variable> rValues =  getVariablesFromRValue(stmt.getRightOp(), methodName);

    return new AssignmentVariables(lValue, rValues);
  }

  private static Set<Variable> getVariablesFromRValue(Value value, String methodName) {
    HashSet<Variable> variables = new HashSet<>();

    // Check if it is a basic variable
    Variable basicVar = getVariable(value, methodName);

    if (basicVar != null)
      variables.add(basicVar);
    else {
      // Not a basic variable, try and extract all
      if (value instanceof JCastExpr) {
        Variable var = getVariable(((JCastExpr) value).getOp(), methodName);
        if (var != null)
          variables.add(var);
      }

      if (value instanceof JStaticInvokeExpr) {
        for (Value arg : ((JStaticInvokeExpr) value).getArgs()) {
          Variable var = getVariable(arg, methodName);
          if (var != null)
            variables.add(var);
        }
      }
    }

    if (value instanceof BinopExpr) {
      Variable var1 = getVariable(((BinopExpr) value).getOp1(), methodName);
      Variable var2 = getVariable(((BinopExpr) value).getOp2(), methodName);

      if (var1 != null)
        variables.add(var1);

      if (var2 != null)
        variables.add(var2);

    }

    return variables;
  }

  private static Variable getVariable(Value value, String methodName) {
    if (value instanceof StaticFieldRef)
      return new Variable(((StaticFieldRef) value).getFieldRef().getSignature(), VariableType.FIELD);

    if (value instanceof JInstanceFieldRef)
      return new Variable(((JInstanceFieldRef) value).getFieldRef().getSignature(), VariableType.FIELD);

    if (value instanceof JimpleLocal)
      return new Variable(methodName + ":" + ((JimpleLocal) value).getName(), VariableType.LOCAL);

    return null;
  }

}
