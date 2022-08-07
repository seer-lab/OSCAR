package oscar.controller.noise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum NoiseHeuristic {
  SYNCHRONIZATION_BASED,
  THREAD_BASED,
  LOCK_BASED,
  MISCELLANEOUS,
  SHARED_VARIABLE_BASED;

  NoiseHeuristic() {}

  public String getShorthand() {
    return NoiseLocation.generateShorthand(name());
  }

  public static NoiseHeuristic fromString(String shorthand) {
    List<NoiseHeuristic> results = Arrays.stream(NoiseHeuristic.values())
                                         .filter(np -> np.getShorthand().equals(shorthand.toLowerCase()))
                                         .collect(Collectors.toList());

    if (results.size() > 1)
      throw new RuntimeException("More than one noise placement categories match shorthand '" + shorthand + "'.");

    if (results.size() == 0)
      throw new RuntimeException("No noise placement categories match shorthand '" + shorthand + "'.");

    return results.get(0);
  }

  public static HashSet<NoiseHeuristic> getAll() {
    return Arrays.stream(values()).collect(Collectors.toCollection(HashSet::new));
  }
}