package oscar;

import oscar.utils.Config;
import soot.*;

import java.util.List;
import java.util.Map;

public class Main {
  public static void main(String[] args) {
    // Parse program configuration
    Config.parse(args);

    // Initial configs

    // Init soot
    //Scene scene = getScene("cflash-data/account", "cflash-data/account/Account.jar");
    Scene scene = FileReader.readClassFile("src/test/java", "SimpleSleepExample");

    // Get all detected Classes
    Map<String, SootClass> sootClasses = ClassReader.getClasses(scene);

    // Get Main class
    //SootClass mainClass = sootClasses.get("Main");
    //SootMethod meth = mainClass.getMethodByName("main");

    SootClass mainClass = sootClasses.get("SimpleSleepExample");
    SootMethod meth = mainClass.getMethodByName("main");

    Body body = meth.retrieveActiveBody();
    List<UnitBox> boxes = body.getAllUnitBoxes();

    System.out.println(body.toString());

  }


}