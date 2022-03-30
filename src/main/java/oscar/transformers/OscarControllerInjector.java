package oscar.transformers;

import oscar.engine.generators.JimpleGenerator;
import oscar.utils.ConfigParser;
import soot.Body;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

import java.util.List;
import java.util.Map;

public class OscarControllerInjector extends CustomTransformer {
  @Override
  protected void internalTransform(Body b, String s, Map<String, String> map) {
    JimpleBody body = (JimpleBody) b;
    JimpleGenerator generator = new JimpleGenerator(body);
    UnitPatchingChain boxes = body.getUnits();

    // Check if class name is Main class name and method body is name
    String className = body.getMethod().getDeclaringClass().getName();
    if (!body.getMethod().isMain())
      return;

    if (!className.equals(ConfigParser.MainClass))
      return;

    // Add Logger initializer statement
    Stmt loggerInitStmt = generator.Statement.staticInvoke("oscar.utils.logger.LoggerFactory", "void initialize()", List.of());
    boxes.insertAfter(loggerInitStmt, boxes.getFirst());

    // Add oscar controller start and end statements
    Stmt initStatement = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void start()", List.of());
    Stmt endStatement = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void end()", List.of());

    boxes.insertAfter(initStatement, loggerInitStmt);
    boxes.insertBefore(endStatement, boxes.getLast());

    b.validate();
  }
}
