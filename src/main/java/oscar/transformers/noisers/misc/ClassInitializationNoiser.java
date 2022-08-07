package oscar.transformers.noisers.misc;

import oscar.controller.noise.NoiseLocation;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleTransformer;
import soot.jimple.Stmt;

import java.util.Set;

public final class ClassInitializationNoiser extends JimpleTransformer {

  public ClassInitializationNoiser() {
    super("cin", ClassInitializationNoiser.class);
    this.routine = this::routine;
  }

  private void routine(JimpleBodyBox bodyBox) {
    // Check if method is an initialization method
    if (!Set.of("<clinit>", "<init>").contains(bodyBox.body().getMethod().getName()))
      return;

    // Insert noise after first non-identity statement
    Stmt firstStmt = bodyBox.body().getFirstNonIdentityStmt();
    bodyBox.body()
           .getUnits()
           .insertBefore(bodyBox.generator().Statement.noise(NoiseLocation.BEFORE_CLASS_INITIALIZATION), firstStmt);
  }
}



