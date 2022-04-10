package oscar.controller.noise;

public class SleepLocation extends InstrumentedLocation {
  private final NoisePlacement noisePlacement;

  public SleepLocation(NoisePlacement noisePlacement) {
    super(InstrumentedLocationType.SLEEP);
    this.noisePlacement = noisePlacement;
  }

  public NoisePlacement getNoisePlacement() {
    return noisePlacement;
  }
}
