package oscar;

import org.apache.commons.cli.ParseException;
import oscar.soot.ClassReader;
import oscar.soot.Soot;
import oscar.utils.ConfigParser;
import oscar.utils.OptionsParser;
import oscar.utils.logger.LoggerFactory;
import soot.*;

import java.util.Map;
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
    Scene scene = Soot.initializeScene();

    // Get all detected Classes
    Map<String, SootClass> sootClasses = ClassReader.getClasses(scene);


    // Get Main class
    //SootClass mainClass = sootClasses.get("Main");
    //SootMethod meth = mainClass.getMethodByName("main");
    /*
    SootClass mainClass = sootClasses.get("SimpleSleepExample");
    SootMethod meth = mainClass.getMethodByName("main");

    Body body = meth.retrieveActiveBody();
    UnitPatchingChain units = body.getUnits();

    SootMethod sleepInst = Scene.v().grabMethod("<java.lang.Thread: void sleep(long)>(2000L)");
    InvokeStmt sleepStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(psLocal, sleepInst.makeRef(), printlnParamter));
    units.add(printlnMethodCallStmt);
  */


    //InstrumentUtil.setupSoot(androidJar, apkPath, outputPath);

  }



}