package oscar.transformers.noisers.thread_based;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.Engine;
import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ThreadCreationTagger extends CustomJimpleTransformer {
  public ThreadCreationTagger() {
    super("jtp", "tct");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    Engine.startTransformer(this.getClass(), body);

    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName()))
      return;

    // Tag all runnable classes launched by thread creation
    getRunnableClasses((JimpleBody) body).forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Tag all runnable methods and lambdas launched by thread creation
    getRunnableMethods((JimpleBody) body).forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Get all thread start and run statements and then wrap them for noising
    List<JInvokeStmt> invokeStmts = getStartAndRunStatements((JimpleBody) body);
    invokeStmts.forEach(s -> wrapThreadLaunch((JimpleBody) body, s));

    body.validate();

    Engine.endTransformer(this.getClass(), body);
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

  private static void wrapThreadLaunch(JimpleBody body, JInvokeStmt threadLaunchStmt) {
    JimpleGenerator generator = new JimpleGenerator(body);
    List<ValueBox> threadLaunchStmtUseBoxes = threadLaunchStmt.getInvokeExprBox().getValue().getUseBoxes();
    JimpleLocal threadLocal = (JimpleLocal) threadLaunchStmtUseBoxes.get(0).getValue();
    SootClass bodyClass = body.getMethod().getDeclaringClass();

    // Create new wrapper method
    SootMethod method = new SootMethod(
        "thread_launch_wrapper_" + Engine.generateRandomString(8),
        List.of(Scene.v().getType("java.lang.Thread")),
        VoidType.v()
    );

    method.setActiveBody(new JimpleBody());
    method.setModifiers(Modifier.STATIC + Modifier.PRIVATE);
    method.setPhantom(false);
    bodyClass.addMethod(method);

    JimpleLocal identityLocal = generator.Local.fromType(Scene.v().getType("java.lang.Thread"));
    ParameterRef paramRef = new ParameterRef(Scene.v().getRefType("java.lang.Thread"), 0);
    JIdentityStmt identityStmt = generator.Statement.identity(identityLocal, paramRef);

    UnitPatchingChain wrapperUnits = method.getActiveBody().getUnits();
    wrapperUnits.add(identityStmt);

    // Create new thread launch statement
    Stmt newThreadLaunchStmt = generator.Statement.virtualInvoke(
        identityLocal,
        threadLaunchStmt.getInvokeExpr().getMethod().getDeclaringClass().getName(),
        threadLaunchStmt.getInvokeExpr().getMethod().getSubSignature(),
        List.of()
    );

    threadLaunchStmtUseBoxes.remove(0);
    threadLaunchStmtUseBoxes.add(new JimpleLocalBox(method.getActiveBody().getParameterLocal(0)));

    // Create an ordered list of all the new statements to be inserted
    List<Unit> newStmts = new ArrayList<>();
    newStmts.addAll(generator.Statement.sleep(NoisePlacement.BEFORE_THREAD_LAUNCH));
    newStmts.addAll(generator.Statement.signal(NoiseCategory.THREAD_BASED));
    newStmts.add(newThreadLaunchStmt);
    newStmts.addAll(generator.Statement.sleep(NoisePlacement.AFTER_THREAD_LAUNCH));
    newStmts.add(new JReturnVoidStmt());

    wrapperUnits.insertAfter(newStmts, identityStmt);

    // Replace original thread run call for wrapper call
    Stmt wrapperCall = generator.Statement.staticInvoke(bodyClass.getName(), method.getSubSignature(), List.of(threadLocal));
    body.getUnits().insertAfter(wrapperCall, threadLaunchStmt);
    body.getUnits().remove(threadLaunchStmt);
  }
}
