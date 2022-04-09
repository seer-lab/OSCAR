package oscar.controller.noise;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public enum NoisePlacement {
  BEFORE_SYNC_BLOCK(NoisePlacementType.SYNC_BASED),
  AFTER_SYNC_BLOCK(NoisePlacementType.SYNC_BASED);

  private final NoisePlacementType type;

  NoisePlacement(NoisePlacementType type) {
    this.type = type;
  }

  public String getShorthand() {
    return type.getShorthand() + generateShorthand(name());
  }

  public NoisePlacementType getType() {
    return type;
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

  public enum NoisePlacementType {
    SYNC_BASED;

    NoisePlacementType() {}

    public String getShorthand() {
      return generateShorthand(name());
    }
  }

  private static String generateShorthand(String name) {
    return Arrays.stream(name.split("_"))
                 .map(s -> s.charAt(0))
                 .map(Object::toString)
                 .map(String::toLowerCase)
                 .reduce("", String::concat);
  }
}

