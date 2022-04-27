package oscar.transformers;

import oscar.engine.CustomJimpleBody;
import oscar.engine.Engine;
import soot.Body;
import soot.BodyTransformer;
import soot.jimple.JimpleBody;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class CustomJimpleTransformer extends BodyTransformer {
  private final String phase;
  private final String subphase;

  private final Class<? extends CustomJimpleTransformer> clazz;
  private final Consumer<CustomJimpleBody> routine;

  public CustomJimpleTransformer(String phase, String subPhase, Class<? extends CustomJimpleTransformer> clazz, Consumer<CustomJimpleBody> routine) {
    this.subphase = phase + "." + subPhase;
    this.phase = phase;
    this.clazz = clazz;
    this.routine = routine;
  }

  @Override
  protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
    if (!(body instanceof JimpleBody))
      throw new RuntimeException("Expected a Jimple body.");

    // First we filter out blacklisted methods
    if (isBodyFiltered(body))
      return;

    CustomJimpleBody customBody = new CustomJimpleBody((JimpleBody) body);

    Engine.startTransformer(clazz, body);

    routine.accept(customBody);
    customBody.v().validate();

    Engine.endTransformer(clazz, body);
  }

  public String getPhase() {
    return phase;
  }

  public String getSubPhase() {
    return subphase;
  }

  protected static boolean isBodyFiltered(Body body) {
    String className = body.getMethod().getDeclaringClass().getName();
    String methodName = body.getMethod().getName();

    boolean isClassBlacklisted = Stream.of(
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

    boolean isMethodNameBlacklisted = Stream.of(
        "<init>"
    ).anyMatch(methodName::equals);

    return isClassBlacklisted || isMethodNameBlacklisted;
  }
}
