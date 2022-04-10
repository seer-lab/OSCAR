package oscar.controller.noise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum NoisePlacement {
  BEFORE_SYNC_BLOCK(NoisePlacementCategory.SYNCHRONIZATION_BASED),
  AFTER_SYNC_BLOCK(NoisePlacementCategory.SYNCHRONIZATION_BASED);

  private final NoisePlacementCategory category;

  NoisePlacement(NoisePlacementCategory category) {
    this.category = category;
  }

  public String getShorthand() {
    return category.getShorthand() + generateShorthand(name());
  }

  public NoisePlacementCategory getCategory() {
    return category;
  }

  public static HashSet<NoisePlacement> getAll() {
    return Arrays.stream(values()).collect(Collectors.toCollection(HashSet::new));
  }

  public static NoisePlacement fromString(String shorthand) {
    List<NoisePlacement> results = Arrays.stream(NoisePlacement.values())
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

