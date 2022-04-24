package oscar.transformers;

import oscar.engine.Engine;
import soot.BodyTransformer;

import java.util.stream.Stream;

public abstract class CustomJimpleTransformer extends BodyTransformer {
  protected final String phase;
  protected final String subphase;

  public CustomJimpleTransformer(String phase, String subphase) {
    this.subphase = phase + "." + subphase;
    this.phase = phase;
  }

  public String getPhase() {
    return phase;
  }

  public String getSubPhase() {
    return subphase;
  }

  protected static boolean isClassBlacklisted(String className) {
    return Stream.of(
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
    ).anyMatch(className::startsWith);
  }
}
