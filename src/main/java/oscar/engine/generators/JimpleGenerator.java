package oscar.engine.generators;

import soot.Unit;
import soot.jimple.JimpleBody;

import java.util.List;

public final class JimpleGenerator {
  public final StatementGenerator Statement;
  public final ConversionGenerator Conversion;
  public final ArithmeticGenerator Arithmetic;
  public final LocalGenerator Local;

  private JimpleBody body;

  public JimpleGenerator(JimpleBody body) {
    this.body = body;

    this.Local = new LocalGenerator(this, body);
    this.Statement = new StatementGenerator(this.Local, body);
    this.Conversion = new ConversionGenerator(this.Local);
    this.Arithmetic = new ArithmeticGenerator(this.Local);
  }
}
