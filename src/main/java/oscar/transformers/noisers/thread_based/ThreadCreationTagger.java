package oscar.transformers.noisers.thread_based;

import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.transformers.noisers.NoiserTag;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;

import java.util.Map;

public final class ThreadCreationTagger extends CustomJimpleTransformer {
  public ThreadCreationTagger() {
    super("jtp", "tct");
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // First we filter out blacklisted methods
    if (isClassBlacklisted(body.getMethod().getDeclaringClass().getName())) return;

    // Tag all runnable classes launched by thread creation
    body.getUnits()
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
        .forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    // Tag all runnable classes launched by thread creation
    body.getUnits()
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
        .forEach(sc -> sc.addTag(NoiserTag.THREAD_LAUNCHED));

    body.validate();
  }
}
