package oscar.controller.noise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum NoiseLocation {
  BEFORE_SYNC_METHOD_CALL(NoiseHeuristic.SYNCHRONIZATION_BASED),
  AFTER_SYNC_METHOD_CALL(NoiseHeuristic.SYNCHRONIZATION_BASED),
  BEFORE_SYNC_BLOCK(NoiseHeuristic.SYNCHRONIZATION_BASED),
  AFTER_SYNC_BLOCK(NoiseHeuristic.SYNCHRONIZATION_BASED),
  //---------------------------------------------------------
  BEFORE_THREAD_LAUNCH(NoiseHeuristic.THREAD_BASED),
  AFTER_THREAD_LAUNCH(NoiseHeuristic.THREAD_BASED),
  BEFORE_THREAD_ROUTINE(NoiseHeuristic.THREAD_BASED),
  //---------------------------------------------------------
  BEFORE_REENTRANT_LOCK_LOCK(NoiseHeuristic.LOCK_BASED),
  AFTER_REENTRANT_LOCK_UNLOCK(NoiseHeuristic.LOCK_BASED),
  //---------------------------------------------------------
  BEFORE_SHARED_FIELD_ACCESS(NoiseHeuristic.SHARED_VARIABLE_BASED),
  BEFORE_SHARED_LOCAL_ACCESS(NoiseHeuristic.SHARED_VARIABLE_BASED),
  //---------------------------------------------------------
  AFTER_SHARED_FIELD_ACCESS(NoiseHeuristic.SHARED_VARIABLE_BASED),
  AFTER_SHARED_LOCAL_ACCESS(NoiseHeuristic.SHARED_VARIABLE_BASED),

  // Experimental
  BEFORE_CLASS_INITIALIZATION(NoiseHeuristic.MISCELLANEOUS),
  BEFORE_THREAD_EXTERNAL_FIELD_REF(NoiseHeuristic.MISCELLANEOUS),
  AFTER_THREAD_EXTERNAL_FIELD_REF(NoiseHeuristic.MISCELLANEOUS);

  private final NoiseHeuristic category;

  NoiseLocation(NoiseHeuristic category) {
    this.category = category;
  }

  public String getShorthand() {
    return category.getShorthand() + generateShorthand(name());
  }

  public NoiseHeuristic getCategory() {
    return category;
  }

  public static HashSet<NoiseLocation> getAll() {
    return Arrays.stream(values()).collect(Collectors.toCollection(HashSet::new));
  }

  public static NoiseLocation fromString(String shorthand) {
    List<NoiseLocation> results = Arrays.stream(NoiseLocation.values())
                                        .filter(np -> np.getShorthand().equals(shorthand.toLowerCase()))
                                        .collect(Collectors.toList());

    if (results.size() > 1)
      throw new RuntimeException("More than one noise placement matches shorthand '" + shorthand + "'.");

    if (results.size() == 0)
      throw new RuntimeException("No noise placement matches shorthand '" + shorthand + "'.");

    return results.get(0);
  }

  static String generateShorthand(String name) {
    return Arrays.stream(name.split("_"))
                 .map(s -> s.charAt(0))
                 .map(Object::toString)
                 .map(String::toLowerCase)
                 .reduce("", String::concat);
  }
}

