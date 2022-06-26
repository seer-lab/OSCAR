package oscar.transformers.analysers;

import jas.Var;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleSceneTransformer;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.*;

import java.util.*;
import java.util.stream.Collectors;

import static oscar.transformers.analysers.Variable.*;

public class SharedVariableAnalyser extends JimpleSceneTransformer {
  private final DefaultDirectedGraph<Variable, DefaultEdge> graph;
  private final HashMap<String, HashSet<String>> sharedVars;

  public SharedVariableAnalyser(HashMap<String, HashSet<String>> sharedVars) {
    super("svn", SharedVariableAnalyser.class);
    this.routine = this::routine;
    this.sharedVars = sharedVars;
    this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
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
      // Get lvalue and rvalue ref
      String methodName = bodyBox.body().getMethod().getName();
      Variable lValueVar = getVariable(assignment.getLeftOp(), methodName);
      Set<Variable> rValueVars = getAllVariablesFromRValue(assignment, methodName);

      // Check if variable is a ref, else try and extract local
      /*
      // Check if variable is a ref, else try and extract local
      if (rValueRef != null) {
        rValueVars.add(new Variable(rValueRef, VariableType.FIELD));
      } else {
        // Try and extract all locals used
        rValueVars = getAllVariablesFromAssignment(assignment, bodyBox.body().getMethod().getName());
      }
     */

      // Add all directed edges to the graph
      if (lValueVar == null || rValueVars.isEmpty())
        continue;

      graph.addVertex(lValueVar);
      for (Variable rValueVar : rValueVars) {
        graph.addVertex(rValueVar);
        graph.addEdge(lValueVar, rValueVar);
      }
    }

    // Check connectivity
    for (Variable var : graph.iterables().vertices()) {
      if (var.getType() == VariableType.FIELD) {
        sharedVars.putIfAbsent(var.getName(), new HashSet<>());
        Set<Variable> reachableVars = getAllReachableEdges(var, graph);

        for (Variable reachableVar : reachableVars)
          if (reachableVar.getType() == VariableType.FIELD)
            sharedVars.get(var.getName()).add(reachableVar.getName());
      }
    }

    printGraph(graph);
    System.out.println();
  }

  private static void printGraph(Graph<Variable, DefaultEdge> graph) {
    // Print out the graph to be sure it's really complete
    Iterator<Variable> iter = new DepthFirstIterator<>(graph);

    while (iter.hasNext()) {
      Variable vertex = iter.next();
      System.out.println(
          "Vertex " + vertex + " is connected to: "
              + graph.edgesOf(vertex).toString());
    }
  }

  public static Set<Variable> getAllVariablesFromRValue(JAssignStmt stmt, String methodName) {
    HashSet<Variable> variables = new HashSet<>();
    Value rightOp = stmt.getRightOp();

    // Check if it is a basic variable
    Variable basicVar = getVariable(rightOp, methodName);

    if (basicVar != null)
      variables.add(basicVar);
    else {
      // Not a basic variable, try and extract all
      if (rightOp instanceof JCastExpr) {
        Variable var = getVariable(((JCastExpr) rightOp).getOp(), methodName);
        if (var != null)
          variables.add(var);
      }

      if (rightOp instanceof JStaticInvokeExpr) {
        for (Value arg : ((JStaticInvokeExpr) rightOp).getArgs()) {
          Variable var = getVariable(arg, methodName);
          if (var != null)
            variables.add(var);
        }
      }
    }

    if (rightOp instanceof BinopExpr) {
      Variable var1 = getVariable(((BinopExpr) rightOp).getOp1(), methodName);
      Variable var2 = getVariable(((BinopExpr) rightOp).getOp2(), methodName);

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

  private HashSet<Variable> getAllReachableEdges(Variable source, Graph<Variable, DefaultEdge> graph) {
    HashSet<Variable> edges = new HashSet<>();

    for (DefaultEdge edge : graph.outgoingEdgesOf(source)) {
      Variable target = graph.getEdgeTarget(edge);

      if (!source.equals(target))
        edges.addAll(getAllReachableEdges(target, graph));

      edges.add(target);
    }

    return edges;
  }
}
