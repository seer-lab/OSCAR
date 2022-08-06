package oscar.transformers.noisers.misc;

import oscar.controller.noise.NoisePlacement;
import oscar.engine.body.JimpleBodyBox;
import oscar.transformers.JimpleTransformer;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
           .insertBefore(bodyBox.generator().Statement.noise(NoisePlacement.BEFORE_CLASS_INITIALIZATION), firstStmt);
  }
}



