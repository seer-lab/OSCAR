package oscar.transformers.injectors;

import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.utils.ConfigParser;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.jimple.parser.node.AStaticModifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ControllerInjector extends CustomJimpleTransformer {
  @Override
  protected void internalTransform(Body oldMainBody, String phaseName, Map<String, String> options) {
    // Check if class name is Main class name and method body is name
    SootClass mainClass = oldMainBody.getMethod().getDeclaringClass();
    String mainClassName = mainClass.getName();

    if (!oldMainBody.getMethod().isMain())
      return;

    if (!mainClassName.equals(ConfigParser.MainClass))
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
    oldMainBody.getUnits().forEach(wrappedMainMethod.getActiveBody().getUnits()::add);
    oldMainBody.getLocals().forEach(wrappedMainMethod.getActiveBody().getLocals()::add);
    oldMainBody.getTraps().forEach(wrappedMainMethod.getActiveBody().getTraps()::add);

    // Create new body for the original (wrapper) main
    JimpleBody newMainBody = new JimpleBody(oldMainBody.getMethod());
    oldMainBody.getMethod().setActiveBody(newMainBody);
    JimpleGenerator newMainGenerator = new JimpleGenerator(newMainBody);
    JimpleLocal mainIdentityLocal = newMainGenerator.Local.arrayFromType(RefType.v("java.lang.String"), 1);
    ParameterRef newMainParamRef = new ParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1),0);
    JIdentityStmt identityStmt = newMainGenerator.Statement.identity(mainIdentityLocal, newMainParamRef);
    newMainBody.getUnits().add(identityStmt);
    oldMainBody.getMethod().setActiveBody(newMainBody);

    // Add Oscar controller routine to parse main arguments and initialize
    JAssignStmt oscarStartStmt = (JAssignStmt) newMainGenerator.Statement.staticInvoke(
        "oscar.controller.Controller",
        "java.lang.String[] start(java.lang.String[])",
        List.of(newMainBody.getParameterLocal(0))
    );

    UnitPatchingChain newMainUnits = newMainBody.getUnits();
    newMainUnits.insertAfter(oscarStartStmt, newMainUnits.getLast());

    // Call old main with parsed args
    Stmt callOrigMainStmt = newMainGenerator.Statement.staticInvoke(
        mainClass.getName(),
        "void main_wrapped(java.lang.String[])",
        List.of(oscarStartStmt.getLeftOp())
    );
    newMainUnits.insertAfter(callOrigMainStmt, newMainUnits.getLast());

    // Insert end statement
    Stmt endStatement = newMainGenerator.Statement.staticInvoke(
        "oscar.controller.Controller",
        "void end()",
        List.of()
    );
    newMainUnits.insertAfter(endStatement, newMainUnits.getLast());

    // Insert return statement at end
    newMainUnits.insertAfter(new JReturnVoidStmt(), newMainUnits.getLast());

    oldMainBody.validate();
    newMainBody.validate();
  }
}
