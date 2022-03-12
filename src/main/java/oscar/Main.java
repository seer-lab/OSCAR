package oscar;

import soot.*;
import soot.jimple.internal.StmtBox;
import soot.options.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) {
    G.reset();

    // Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    List<String> processDirs = new ArrayList<>();
    processDirs.add("cflash-data/account/Account.jar");
    Options.v().set_soot_classpath("cflash-data/account");
    Options.v().set_process_dir(processDirs);
    Options.v().set_whole_program(true);

    Scene.v().loadNecessaryClasses();

    HashMap<String, SootClass> sootClasses = new HashMap<>(
        Scene.v().getClasses().stream()
            .filter(sc -> !sc.getName().startsWith("java."))
            .filter(sc -> !sc.getName().startsWith("sun."))
            .filter(sc -> !sc.getName().startsWith("jdk."))
            .filter(sc -> !sc.getName().startsWith("javax."))
            .filter(sc -> !sc.getName().startsWith("com.sun"))
            .collect(Collectors.toMap(SootClass::getName, Function.identity()))
    );
    SootClass mainClass = sootClasses.get("Main");
    SootMethod meth = mainClass.getMethodByName("main");

    Body body = meth.retrieveActiveBody();
    List<UnitBox> boxes = body.getAllUnitBoxes();
  }

  public void traverseUnits() {

  }
}