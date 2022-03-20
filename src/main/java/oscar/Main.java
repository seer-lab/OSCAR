package oscar;

import org.apache.commons.cli.ParseException;
import oscar.engine.Engine;
import oscar.engine.transformers.RandomNoiserTransformer;
import oscar.utils.ConfigParser;
import oscar.utils.OptionsParser;
import oscar.utils.logger.LoggerFactory;
import soot.*;

import java.util.List;
import java.util.logging.Logger;

public class Main {
  public static void main(String[] args) {
    // Parse program CLI
    try {
      OptionsParser.parse(args);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }

    // Parse program configuration
    ConfigParser.parse(OptionsParser.PropertiesFile);
    Logger logger = LoggerFactory.getInstance(Main.class);

    // Init soot
    Engine.initialize();

    // Register transformers
    List<Transform> transformers = List.of(
        new Transform("jtp.rnt", new RandomNoiserTransformer())
    );

    transformers.forEach(PackManager.v().getPack("jtp")::add);

    // Run Soot packs (note that our transformer pack is added to the phase "jtp")
    PackManager.v().runPacks();

    // Write the result of packs in outputPath
    PackManager.v().writeOutput();

   /*
    // Get all detected Classes
    Map<String, SootClass> sootClasses = ClassReader.getClasses(scene);

    // Get Main class
    SootClass mainClass = sootClasses.get(ConfigParser.MainClass);
    SootMethod meth = mainClass.getMethodByName("main");
    GrimpBody body = (GrimpBody) meth.getActiveBody();

    System.out.println("-------------BODY-------------");
    System.out.println(body);
    System.out.println("------------------------------");

    UnitPatchingChain units = body.getUnits();

    units.stream()
         .filter(u -> u.getDefBoxes().contains("staticinvoke"))
         .forEach(System.out::println);

    //SootMethod sleepInst = Scene.v().grabMethod("<java.lang.Thread: void sleep(long)>(2000L)");
    // InvokeStmt sleepStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, sleepInst.makeRef(), printlnParamter));
    // units.add(printlnMethodCallStmt);

    //InstrumentUtil.setupSoot(androidJar, apkPath, outputPath);
    */

  }
}