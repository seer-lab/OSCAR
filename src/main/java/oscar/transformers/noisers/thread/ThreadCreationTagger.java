package oscar.transformers.noisers.thread;

import oscar.controller.noise.NoiseCategory;
import oscar.controller.noise.NoisePlacement;
import oscar.engine.CustomJimpleBody;
import oscar.engine.Engine;
import oscar.transformers.JimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ThreadCreationTagger extends JimpleTransformer {
  public ThreadCreationTagger() {
    super("jtp", "tct", ThreadCreationTagger.class, ThreadCreationTagger::routine);
  }

  private static void routine(CustomJimpleBody body) {
    // Tag all runnable classes launched by thread creation
    getRunnableClasses(body.v()).forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Tag all runnable methods and lambdas launched by thread creation
    getRunnableMethods(body.v()).forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Get all thread start and run statements and then wrap them for noising
    List<JInvokeStmt> invokeStmts = getStartAndRunStatements(body.v());

    // Replace original calls with calls to wrapped method
    for (JInvokeStmt stmt : invokeStmts) {
      body.v().getUnits().insertBefore(body.g().Statement.sleep(NoisePlacement.BEFORE_THREAD_LAUNCH), stmt);
      body.v().getUnits().insertBefore(body.g().Statement.signal(NoiseCategory.THREAD_BASED), stmt);
      body.v().getUnits().insertAfter(body.g().Statement.sleep(NoisePlacement.AFTER_THREAD_LAUNCH), stmt);
      //body.v().getUnits().insertAfter(wrapThreadLaunch(body, stmt), stmt);
      //body.v().getUnits().remove(stmt);
    }
  }

  private static List<SootClass> getRunnableClasses(JimpleBody body) {
    return body.getUnits()
               .stream()
               .filter(JInvokeStmt.class::isInstance)
               .map(JInvokeStmt.class::cast)
               .map(JInvokeStmt::getInvokeExpr)
               .filter(JSpecialInvokeExpr.class::isInstance)
               .map(JSpecialInvokeExpr.class::cast)
               .filter(e -> e.getMethod().getDeclaringClass().getName().equals("<java.lang.Thread"))
               .filter(e -> e.getMethod().getName().equals("<init>"))
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

  private static Stmt wrapThreadLaunch(CustomJimpleBody body, JInvokeStmt threadLaunchStmt) {
    List<ValueBox> threadLaunchStmtUseBoxes = threadLaunchStmt.getInvokeExprBox().getValue().getUseBoxes();
    JimpleLocal threadLocal = (JimpleLocal) threadLaunchStmtUseBoxes.get(0).getValue();
    SootClass bodyClass = body.v().getMethod().getDeclaringClass();

    // Create new wrapper method
    SootMethod wrapperMethod = new SootMethod("thread_launch_wrapper_" + Engine.generateRandomString(8),
                                              List.of(Scene.v().getType("java.lang.Thread")), VoidType.v()
    );

    CustomJimpleBody wrapperBody = new CustomJimpleBody(new JimpleBody());

    wrapperMethod.setActiveBody(wrapperBody.v());
    wrapperMethod.setModifiers(Modifier.STATIC + Modifier.PRIVATE);
    wrapperMethod.setPhantom(false);
    bodyClass.addMethod(wrapperMethod);

    JimpleLocal identityLocal = wrapperBody.g().Local.fromType(Scene.v().getType("java.lang.Thread"));
    ParameterRef paramRef = new ParameterRef(Scene.v().getRefType("java.lang.Thread"), 0);
    JIdentityStmt identityStmt = wrapperBody.g().Statement.identity(identityLocal, paramRef);

    UnitPatchingChain wrapperUnits = wrapperMethod.getActiveBody().getUnits();
    wrapperUnits.add(identityStmt);

    // Create new thread launch statement
    Stmt newThreadLaunchStmt = wrapperBody.g().Statement.virtualInvoke(
        identityLocal,
        threadLaunchStmt.getInvokeExpr()
                        .getMethod()
                        .getDeclaringClass()
                        .getName(),
        threadLaunchStmt.getInvokeExpr()
                        .getMethod()
                        .getSubSignature(),
        List.of()
    );

    threadLaunchStmtUseBoxes.remove(0);
    threadLaunchStmtUseBoxes.add(new JimpleLocalBox(wrapperMethod.getActiveBody().getParameterLocal(0)));

    // Create an ordered list of all the new statements to be inserted
    List<Unit> newStmts = new ArrayList<>();
    newStmts.addAll(wrapperBody.g().Statement.sleep(NoisePlacement.BEFORE_THREAD_LAUNCH));
    newStmts.addAll(wrapperBody.g().Statement.signal(NoiseCategory.THREAD_BASED));
    newStmts.add(newThreadLaunchStmt);
    newStmts.addAll(wrapperBody.g().Statement.sleep(NoisePlacement.AFTER_THREAD_LAUNCH));
    newStmts.add(new JReturnVoidStmt());

    wrapperUnits.insertAfter(newStmts, identityStmt);

    wrapperBody.v().validate();

    // Replace original thread run call for wrapper call
    return body.g().Statement.staticInvoke(bodyClass.getName(), wrapperMethod.getSubSignature(), List.of(threadLocal));
  }
}
