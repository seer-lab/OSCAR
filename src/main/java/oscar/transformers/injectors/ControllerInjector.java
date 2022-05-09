package oscar.transformers.injectors;

import oscar.engine.CustomJimpleBody;
import oscar.engine.Engine;
import oscar.transformers.JimpleTransformer;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.*;

import java.util.List;

public final class ControllerInjector extends JimpleTransformer {
  public ControllerInjector() {
    super("jtp", "ci", ControllerInjector.class, ControllerInjector::routine);
  }

  private static void routine(CustomJimpleBody oldBody) {
    // Check if class name is Main class name and method body is name
    SootClass mainClass = oldBody.v().getMethod().getDeclaringClass();
    String mainClassName = mainClass.getName();

    if (!oldBody.v().getMethod().isMain())
      return;

    if (!mainClassName.equals(Engine.getMainClass()))
      return;

    // Create new main method with original main's active body to then be wrapped
    SootMethod wrappedMainMethod = new SootMethod(
        "main_wrapped",
        List.of(ArrayType.v(RefType.v("java.lang.String"), 1)),
        VoidType.v()
    );

    wrappedMainMethod.setModifiers(Modifier.STATIC + Modifier.PUBLIC);
    wrappedMainMethod.setPhantom(false);
    mainClass.addMethod(wrappedMainMethod);

    // Copy statements from old main to new wrapped main
    wrappedMainMethod.setActiveBody(new JimpleBody());
    oldBody.v().getUnits().forEach(wrappedMainMethod.getActiveBody().getUnits()::add);
    oldBody.v().getLocals().forEach(wrappedMainMethod.getActiveBody().getLocals()::add);
    oldBody.v().getTraps().forEach(wrappedMainMethod.getActiveBody().getTraps()::add);

    // Create new body for the original (wrapper) main
    CustomJimpleBody newBody = new CustomJimpleBody(new JimpleBody());
    oldBody.v().getMethod().setActiveBody(newBody.v());
    JimpleLocal mainIdentityLocal = newBody.g().Local.arrayFromType(RefType.v("java.lang.String"), 1);
    ParameterRef newMainParamRef = new ParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0);
    JIdentityStmt identityStmt = newBody.g().Statement.identity(mainIdentityLocal, newMainParamRef);
    newBody.v().getUnits().add(identityStmt);

    // Add Oscar controller routine to parse main arguments and initialize
    JAssignStmt oscarStartStmt = (JAssignStmt) newBody.g().Statement.staticInvoke(
        "oscar.controller.Controller",
        "java.lang.String[] start(java.lang.String[])",
        List.of(newBody.v().getParameterLocal(0))
    );

    UnitPatchingChain newMainUnits = newBody.v().getUnits();
    newMainUnits.insertAfter(oscarStartStmt, newMainUnits.getLast());

    // Call old main with parsed args
    Stmt callOrigMainStmt = newBody.g().Statement.staticInvoke(
        mainClass.getName(),
        "void main_wrapped(java.lang.String[])",
        List.of(oscarStartStmt.getLeftOp())
    );
    newMainUnits.insertAfter(callOrigMainStmt, newMainUnits.getLast());

    // Insert end statement
    Stmt endStatement = newBody.g().Statement.staticInvoke(
        "oscar.controller.Controller",
        "void end()",
        List.of()
    );
    newMainUnits.insertAfter(endStatement, newMainUnits.getLast());

    // Insert return statement at end
    newMainUnits.insertAfter(new JReturnVoidStmt(), newMainUnits.getLast());

    oldBody.v().validate();
    newBody.v().validate();
  }
}
