package oscar.transformers;

import oscar.engine.Engine;
import oscar.engine.body.JimpleBodyBox;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public abstract class JimpleSceneTransformer extends SceneTransformer {
  private final String phase = "wjtp";
  private final String subphase;

  private final Class<? extends JimpleSceneTransformer> clazz;
  private final Consumer<JimpleBodyBox> routine;

  private final HashSet<String> visited = new HashSet<>();

  public JimpleSceneTransformer(String subPhase, Class<? extends JimpleSceneTransformer> clazz, Consumer<JimpleBodyBox> routine) {
    this.subphase = phase + "." + subPhase;
    this.clazz = clazz;
    this.routine = routine;
  }

  @Override
  protected void internalTransform(String phaseName, Map<String, String> options) {
    Engine.startSceneTransformer(clazz);

    recursiveTransform(Scene.v().getMainMethod());

    Engine.endSceneTransformer(clazz);
  }

  private void recursiveTransform(SootMethod method) {
    // Avoid visiting same method multiple times
    if (visited.contains(getMethodFullName(method)))
      return;

    visited.add(getMethodFullName(method));

    // Avoid blacklisted methods/classes
    if (Engine.isClassBlacklisted(method))
      return;

    Engine.startCallgraphRoutine(clazz, method);

    // Run routine
    routine.accept(new JimpleBodyBox((JimpleBody) method.getActiveBody()));

    // Get all edges of this method in call graph
    Iterator<MethodOrMethodContext> targets = new Targets(Scene.v().getCallGraph().edgesOutOf(method));

    while (targets.hasNext())
      recursiveTransform((SootMethod) targets.next());

    Engine.endCallgraphRoutine(clazz, method);
  }

  public String getPhase() {
    return phase;
  }

  public String getSubPhase() {
    return subphase;
  }

  private static String getMethodFullName(SootMethod method) {
    return method.getDeclaringClass() + " " + method.getSignature();
  }
}
