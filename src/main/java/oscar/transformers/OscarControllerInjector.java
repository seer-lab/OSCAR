package oscar.transformers;

import oscar.engine.generators.JimpleGenerator;
import oscar.utils.ConfigParser;
import soot.Body;
import soot.UnitPatchingChain;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import java.util.List;
import java.util.Map;

public class OscarControllerInjector extends CustomJimpleTransformer {
  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // Check if class name is Main class name and method body is name
    String className = body.getMethod().getDeclaringClass().getName();
    if (!body.getMethod().isMain())
      return;

    if (!className.equals(ConfigParser.MainClass))
      return;

    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    // Add commander Parse

    // Add Logger initializer statement
    Stmt loggerInitStmt = generator.Statement.staticInvoke("oscar.utils.logger.LoggerFactory", "void initialize()", List.of());
    body.getUnits().insertAfter(loggerInitStmt, body.getUnits().getFirst());

    // Add oscar controller start and end statements
    Stmt initStatement = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void start()", List.of());
    Stmt endStatement = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void end()", List.of());

    body.getUnits().insertAfter(initStatement, loggerInitStmt);
    body.getUnits().insertBefore(endStatement, body.getUnits().getLast());

    body.validate();
  }
}
