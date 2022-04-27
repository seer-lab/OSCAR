package oscar.engine;

import oscar.engine.generators.JimpleGenerator;
import soot.jimple.JimpleBody;

public final class CustomJimpleBody {
  private final JimpleBody body;
  private final JimpleGenerator generator;

  public CustomJimpleBody(JimpleBody body) {
    this.body = body;
    this.generator = new JimpleGenerator(body);
  }

  public JimpleBody v() {
    return body;
  }

  public JimpleGenerator g() {
    return generator;
  }
}
