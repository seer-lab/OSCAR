package oscar.controller.noise;

public class SignalLocation extends InstrumentedLocation {
  private final NoisePlacement.NoisePlacementCategory noisePlacementCategory;

  public SignalLocation(NoisePlacement.NoisePlacementCategory noisePlacementCategory) {
    super(InstrumentedLocationType.SIGNAL);
    this.noisePlacementCategory = noisePlacementCategory;
  }

  public NoisePlacement.NoisePlacementCategory getNoisePlacementCategory() {
    return noisePlacementCategory;
  }
}
