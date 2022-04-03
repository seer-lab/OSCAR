package oscar.controller.noise;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum NoisePlacement {
  SYNC_BASED_BEFORE_SYNC_BLOCK,
  SYNC_BASED_AFTER_SYNC_BLOCK;

  private final String shorthand;

  NoisePlacement() {
    this.shorthand = Arrays.stream(name().split("_"))
                           .map(s -> s.charAt(0))
                           .map(Object::toString)
                           .map(String::toLowerCase)
                           .reduce("", String::concat);
  }

  public String getShorthand() {
    return shorthand;
  }

  public static NoisePlacement fromString(String shorthand) {
    List<NoisePlacement> results = Arrays.stream(NoisePlacement.values())
                                         .filter(np -> np.shorthand.equals(shorthand.toLowerCase()))
                                         .collect(Collectors.toList());

    if (results.size() > 1)
      throw new RuntimeException("More than one noise placement type matches shorthand.");

    if (results.size() == 0)
      throw new RuntimeException("No noise placement matches shorthand '" + shorthand + "'.");

    return results.get(0);
  }
}

