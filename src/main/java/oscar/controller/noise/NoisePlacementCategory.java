package oscar.controller.noise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum NoisePlacementCategory {
  SYNCHRONIZATION_BASED;

  NoisePlacementCategory() {}

  public String getShorthand() {
    return NoisePlacement.generateShorthand(name());
  }

  public static NoisePlacementCategory fromString(String shorthand) {
    List<NoisePlacementCategory> results = Arrays.stream(NoisePlacementCategory.values())
                                                 .filter(np -> np.getShorthand().equals(shorthand.toLowerCase()))
                                                 .collect(Collectors.toList());

    if (results.size() > 1)
      throw new RuntimeException("More than one noise placement categories match shorthand '" + shorthand + "'.");

    if (results.size() == 0)
      throw new RuntimeException("No noise placement categories match shorthand '" + shorthand + "'.");

    return results.get(0);
  }

  public static HashSet<NoisePlacementCategory> getAll() {
    return Arrays.stream(values()).collect(Collectors.toCollection(HashSet::new));
  }
}