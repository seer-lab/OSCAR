package oscar.soot;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.util.List;

public final class FileReader {
  public static Scene readJar(String classpath, String directory) {
    return readJar(classpath, List.of(directory));
  }

  public static Scene readJar(String classpath, List<String> directories) {
    G.reset();

    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    Options.v().set_whole_program(true);

    Options.v().set_soot_classpath(classpath);
    Options.v().set_process_dir(directories);

    Scene.v().loadNecessaryClasses();

    return Scene.v();
  }

  public static Scene readClassFile(String classpath, String mainClass) {
    G.reset();

    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);

    Options.v().set_soot_classpath(classpath);
    SootClass sc = Scene.v().loadClassAndSupport(mainClass);
    sc.setApplicationClass();

    Scene.v().loadNecessaryClasses();

    return Scene.v();
  }
}
