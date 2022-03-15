package oscar;

import oscar.utils.LoggerFactory;
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

  private static final List<String> BlacklistedClasses = List.of(
      "java.",
      "sun.",
      "jdk.",
      "javax.",
      "com.sun"
  );

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

    return sceneClasses
        .stream()
        .filter(sc -> {
          boolean condition = !BlacklistedClasses.contains(sc.getName());

          if (condition)
            logger.info("Class detected: %s".formatted(sc.getName()));
          else
            logger.info("Class detected (ignored): %s".formatted(sc.getName()));

          return condition;
        })
        .collect(Collectors.toMap(SootClass::getName, Function.identity()));
  }
}
