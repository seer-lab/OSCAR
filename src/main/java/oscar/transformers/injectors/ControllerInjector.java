package oscar.transformers.injectors;

import oscar.engine.generators.JimpleGenerator;
import oscar.transformers.CustomJimpleTransformer;
import oscar.utils.ConfigParser;
import soot.Body;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import java.util.List;
import java.util.Map;

public class ControllerInjector extends CustomJimpleTransformer {
  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    // Check if class name is Main class name and method body is name
    String className = body.getMethod().getDeclaringClass().getName();
    if (!body.getMethod().isMain())
      return;

    if (!className.equals(ConfigParser.MainClass))
      return;

    JimpleGenerator generator = new JimpleGenerator((JimpleBody) body);

    // Add oscar controller start and end statements
    Stmt initStatement = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void start()", List.of());
    Stmt endStatement = generator.Statement.staticInvoke("oscar.controllers.OscarController", "void end()", List.of());

    body.getUnits().insertAfter(initStatement, body.getUnits().getFirst());
    body.getUnits().insertBefore(endStatement, body.getUnits().getLast());

    body.validate();
  }
}
