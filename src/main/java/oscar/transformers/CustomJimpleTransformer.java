package oscar.transformers;

import oscar.engine.generators.JimpleGenerator;
import soot.Body;
import soot.BodyTransformer;
import soot.UnitPatchingChain;
import soot.jimple.JimpleBody;

import java.util.List;
import java.util.Map;

public abstract class CustomJimpleTransformer extends BodyTransformer {
  protected static final List<String> blacklistedClasses = List.of(
      "java.",
      "sun.",
      "jdk.",
      "javax.",
      "com.",
      "org.",
      "kotlin.",
      "android.",
      "io.",
      "okhttp3.",
      "dagger.",
      "soot.",
      "oscar.",
      "$"
  );

  protected static boolean isClassBlacklisted(String className) {
    return blacklistedClasses.stream().noneMatch(className::startsWith);
  }
}
