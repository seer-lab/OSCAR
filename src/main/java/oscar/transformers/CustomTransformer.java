package oscar.transformers;

import soot.Body;
import soot.BodyTransformer;

import java.util.List;
import java.util.Map;

public abstract class CustomTransformer extends BodyTransformer {
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
      "$"
  );

  protected static boolean isClassBlacklisted(String className) {
    return blacklistedClasses.stream().noneMatch(className::startsWith);
  }
}
