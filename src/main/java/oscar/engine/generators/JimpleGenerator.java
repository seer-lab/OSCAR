package oscar.engine.generators;

import soot.*;
import soot.javaToJimple.DefaultLocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;

public final class JimpleGenerator {
  public final StatementGenerator Statement;
  public final ConversionGenerator Conversion;
  public final ArithmeticGenerator Arithmetic;
  public final LocalGenerator Local;

  public JimpleGenerator(JimpleBody body) {
    this.Local = new LocalGenerator(this, body);
    this.Statement = new StatementGenerator(this.Local, body);
    this.Conversion = new ConversionGenerator(this.Local);
    this.Arithmetic = new ArithmeticGenerator(this.Local);
  }
}
