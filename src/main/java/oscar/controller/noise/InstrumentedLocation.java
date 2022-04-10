package oscar.controller.noise;

import java.util.UUID;

public abstract class InstrumentedLocation {
  private final InstrumentedLocationType type;
  private final String uuid;

  public InstrumentedLocation(InstrumentedLocationType type) {
    this.uuid = UUID.randomUUID().toString();
    this.type = type;
  }

  public String getUUID() {
    return uuid;
  }

  public InstrumentedLocationType getType() {
    return type;
  }

  public enum InstrumentedLocationType {
    SLEEP,
    SIGNAL
  }
}
