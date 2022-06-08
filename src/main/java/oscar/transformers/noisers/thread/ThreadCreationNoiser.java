package oscar.transformers.noisers.thread;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleSceneTransformer;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.*;
import soot.tagkit.StringConstantValueTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ThreadCreationNoiser extends JimpleSceneTransformer {
  private static final StringConstantValueTag THREAD_LAUNCH_NOISED = new StringConstantValueTag("THREAD_LAUNCH_NOISED");

  public ThreadCreationNoiser() {
    super("tcn", ThreadCreationNoiser.class, ThreadCreationNoiser::routine);
  }

  private static void routine(JimpleBodyBox bodyBox) {
    HashSet<SootClass> runnableClasses = new HashSet<>();

    // Tag all runnable classes launched by thread creation
    runnableClasses.addAll(getRunnableClasses(bodyBox.body()));

    // Tag all runnable methods and lambdas launched by thread creation
    runnableClasses.addAll(getRunnableMethods(bodyBox.body()));

    // Tag all classes that extend the thread interface and are launched
    runnableClasses.addAll(getThreadExtendingClasses(bodyBox.body()));

    // Noise all runnable classes' run methods
    runnableClasses.forEach(ThreadCreationNoiser::noiseThreadRoutine);

    // Get all thread start and run statements and then wrap them for noising
    List<JInvokeStmt> invokeStmts = getStartAndRunStatements(bodyBox.body());

    //Replace original calls with calls to wrapped method
    for (JInvokeStmt stmt : invokeStmts) {
      bodyBox.body()
             .getUnits()
             .insertBefore(bodyBox.generator().Statement.noise(NoisePlacement.BEFORE_THREAD_LAUNCH), stmt);
      bodyBox.body().getUnits().insertBefore(bodyBox.generator().Statement.signal(NoiseCategory.THREAD_BASED), stmt);
      bodyBox.body()
             .getUnits()
             .insertAfter(bodyBox.generator().Statement.noise(NoisePlacement.AFTER_THREAD_LAUNCH), stmt);
    }
  }

  private static void noiseThreadRoutine(SootClass sootClass) {
    JimpleBody classBody = (JimpleBody) sootClass.getMethod("void run()").getActiveBody();
    JimpleBodyBox bodyBox = new JimpleBodyBox(classBody);

    // Check if body has already been instrumented
    if (bodyBox.body().hasTag(THREAD_LAUNCH_NOISED.getName()))
      return;

    // Insert noise and signal statements after first (identity statement)
    List<Unit> noiseStmts = new ArrayList<>(bodyBox.generator().Statement.noise(NoisePlacement.BEFORE_THREAD_ROUTINE));
    noiseStmts.addAll(bodyBox.generator().Statement.signal(NoiseCategory.THREAD_BASED));

    bodyBox.body().getUnits().insertBefore(noiseStmts, bodyBox.body().getFirstNonIdentityStmt());

    // Add instrumented tag and validate body
    bodyBox.body().addTag(THREAD_LAUNCH_NOISED);

    bodyBox.body().validate();
  }

  private static List<SootClass> getRunnableClasses(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(JInvokeStmt.class::cast)
               .map(JInvokeStmt::getInvokeExpr)
               .filter(JSpecialInvokeExpr.class::isInstance)
               .map(JSpecialInvokeExpr.class::cast)
               .filter(e -> e.getMethod().getDeclaringClass().getName().equals("java.lang.Thread"))
               .filter(e -> e.getMethod().getName().equals("<init>"))
               .filter(e -> e.getArgCount() != 0)
               .map(e -> e.getArg(0))
               .filter(JimpleLocal.class::isInstance)
               .map(JimpleLocal.class::cast)
               .map(JimpleLocal::getType)
               .filter(RefType.class::isInstance)
               .map(RefType.class::cast)
               .map(RefType::getSootClass)
               .filter(c -> !c.getName().equals("java.lang.Runnable")) // This blacklists lambdas
               .collect(Collectors.toList());
  }

  private static List<SootClass> getThreadExtendingClasses(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(JInvokeStmt.class::cast)
               .map(JInvokeStmt::getInvokeExpr)
               .filter(JSpecialInvokeExpr.class::isInstance)
               .map(JSpecialInvokeExpr.class::cast)
               .map(JSpecialInvokeExpr::getMethod)
               .filter(m -> m.getName().equals("<init>"))
               .map(SootMethod::getDeclaringClass)
               .filter(SootClass::hasSuperclass)
               .filter(c -> c.getSuperclass().getName().equals("java.lang.Thread"))
               .map(SootClass::getType)
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
               .filter(ThreadCreationNoiser::isThreadStartOrRunStatement)
               .collect(Collectors.toList());
  }

  private static boolean isThreadStartOrRunStatement(JInvokeStmt stmt) {
    SootMethodRef ref = stmt.getInvokeExpr().getMethodRef();
    String methodName = ref.getName();
    String className = ref.getDeclaringClass().getName();

    return List.of("start", "run").contains(methodName) && className.equals("java.lang.Thread");
  }
}



