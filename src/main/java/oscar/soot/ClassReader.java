package oscar.soot;

import oscar.utils.logger.LoggerFactory;
import soot.Scene;
import soot.SootClass;
import soot.util.Chain;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ClassReader {
  private static final Logger logger = LoggerFactory.getInstance(ClassReader.class);


  /**
   * Detect and obtain all the clases from examined files
   *
   * @param scene current scene instance of Soot
   * @return map of all classes detected
   */
  public static Map<String, SootClass> getClasses(Scene scene) {
    logger.info("Detecting classes from files.");
    Chain<SootClass> sceneClasses = scene.getClasses();
    logger.info("%d classes detected.".formatted(sceneClasses.size()));

    Map<String, SootClass> filteredSceneClasses = sceneClasses
        .stream()
        /* .filter(sc -> {
         boolean condition = BlacklistedClasses.stream().noneMatch(bc -> sc.getName().startsWith(bc));

          if (condition)
            logger.fine("Class detected: %s".formatted(sc.getName()));
          else
            logger.fine("Class detected (ignored): %s".formatted(sc.getName()));

          return condition;
        })*/
        .collect(Collectors.toMap(SootClass::getName, Function.identity()));

    logger.info("Filtered detected class count: %d/%d.".formatted(filteredSceneClasses.size(), sceneClasses.size()));

    return filteredSceneClasses;
  }
}
