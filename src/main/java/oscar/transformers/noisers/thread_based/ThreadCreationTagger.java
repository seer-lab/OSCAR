package oscar.transformers.noisers.thread_based;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ThreadCreationTagger extends CustomJimpleTransformer {
  public ThreadCreationTagger() {
    super("jtp", "tct");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Tag all runnable classes launched by thread creation
    getRunnableClasses((JimpleBody) body).forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Tag all runnable methods and lambdas launched by thread creation
    getRunnableMethods((JimpleBody) body).forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Get all thread start and run statements for noising
    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    List<JInvokeStmt> invokeStmts = getStartAndRunStatements((JimpleBody) body);

    for (JInvokeStmt stmt : invokeStmts) {
      body.getUnits().insertBefore(generator.Statement.sleep(NoisePlacement.BEFORE_THREAD_LAUNCH), stmt);
      body.getUnits().insertBefore(generator.Statement.signal(NoiseCategory.THREAD_BASED), stmt);

      body.getUnits().insertAfter(generator.Statement.sleep(NoisePlacement.AFTER_THREAD_LAUNCH), stmt);
    }

    body.validate();
  }

  private static List<SootClass> getRunnableClasses(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(JInvokeStmt.class::cast)
               .map(JInvokeStmt::getInvokeExpr)
               .filter(JSpecialInvokeExpr.class::isInstance)
               .map(JSpecialInvokeExpr.class::cast)
               .filter(e -> e.getMethod().getSignature().equals("<java.lang.Thread: void <init>(java.lang.Runnable)>"))
               .filter(e -> e.getArgCount() != 0)
               .map(e -> e.getArg(0))
               .filter(JimpleLocal.class::isInstance)
               .map(JimpleLocal.class::cast)
               .map(JimpleLocal::getType)
               .filter(RefType.class::isInstance)
               .map(RefType.class::cast)
               .map(RefType::getSootClass)
               .collect(Collectors.toList());
  }

  private static List<SootClass> getRunnableMethods(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JAssignStmt.class::isInstance)
               .map(JAssignStmt.class::cast)
               .map(JAssignStmt::getRightOp)
               .filter(JStaticInvokeExpr.class::isInstance)
               .map(JStaticInvokeExpr.class::cast)
               .map(JStaticInvokeExpr::getMethodRef)
               .filter(sie -> sie.getReturnType() instanceof RefType)
               .filter(sie -> ((RefType) sie.getReturnType()).getClassName().equals("java.lang.Runnable"))
               .map(SootMethodInterface::getDeclaringClass)
               .collect(Collectors.toList());
  }

  private static List<JInvokeStmt> getStartAndRunStatements(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(JInvokeStmt.class::cast)
               .filter(s -> s.getInvokeExpr() instanceof JVirtualInvokeExpr)
               .filter(ThreadCreationTagger::isThreadStartOrRunStatement)
               .collect(Collectors.toList());
  }

  private static boolean isThreadStartOrRunStatement(JInvokeStmt stmt) {
    SootMethodRef ref = stmt.getInvokeExpr().getMethodRef();
    String methodName = ref.getName();
    String className = ref.getDeclaringClass().getName();

    return List.of("start", "run").contains(methodName) && className.equals("java.lang.Thread");
  }
}
