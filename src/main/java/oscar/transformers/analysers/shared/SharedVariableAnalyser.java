package oscar.transformers.analysers.shared;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleSceneTransformer;
import oscar.transformers.analysers.AssignmentVariables;
import oscar.transformers.analysers.Variable;
import oscar.transformers.analysers.VariableType;
import soot.jimple.internal.*;

import java.util.*;
import java.util.stream.Collectors;

public class SharedVariableAnalyser extends JimpleSceneTransformer {
  private final HashMap<String, HashSet<String>> variableDependencies;

  public SharedVariableAnalyser(HashMap<String, HashSet<String>> variableDependencies) {
    super("sva", SharedVariableAnalyser.class);
    this.routine = this::routine;
    this.variableDependencies = variableDependencies;
  }

  private void routine(JimpleBodyBox bodyBox) {
    DefaultDirectedGraph<Variable, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

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
      AssignmentVariables assignmentVariables = Variable.getVariablesFromAssignment(assignment, bodyBox);

      Variable lValueVar = assignmentVariables.getLValue();
      Set<Variable> rValueVars = assignmentVariables.getRValues();

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
      variableDependencies.putIfAbsent(var.getName(), new HashSet<>());

      HashSet<Variable> dependencies = getAllDependencies(var, graph, new HashSet<>());
      for (Variable dependency : dependencies)
        if (dependency.getType() == VariableType.FIELD)
          variableDependencies.get(var.getName()).add(dependency.getName());
    }
  }

  private static void printGraph(Graph<Variable, DefaultEdge> graph) {
    // Print out the graph to be sure it's really complete
    Iterator<Variable> iter = new DepthFirstIterator<>(graph);

    while (iter.hasNext()) {
      Variable vertex = iter.next();
      System.out.print(vertex.getName() + " is connected to: ");
      for (DefaultEdge edge : graph.outgoingEdgesOf(vertex))
        System.out.print(graph.getEdgeTarget(edge).getName() + " ");
      System.out.println();
      System.out.println();
    }
  }

  private HashSet<Variable> getAllDependencies(Variable source, Graph<Variable, DefaultEdge> graph, HashSet<Variable> vertices) {
    Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(source);

    for (DefaultEdge edge : outgoingEdges) {
      Variable target = graph.getEdgeTarget(edge);

      if (!vertices.contains(target)) {
        vertices.add(target);
        vertices.addAll(getAllDependencies(target, graph, vertices));
      }
    }

    return vertices;
  }
}
