/**
 * Taken from: "Java 9 Concurrency Cookbook - Second Edition"
 */

public class ParkingStats {

	/**
	 * This two variables store the number of cars and motorcycles in the parking
	 */
	private long numberCars;
	private long numberMotorcycles;

	/**
	 * Two objects for the synchronization. ControlCars synchronizes the access to
	 * the numberCars attribute and controlMotorcycles synchronizes the access to
	 * the numberMotorcycles attribute.
	 */
	private final Object controlCars, controlMotorcycles;

	private ParkingCash cash;

	private int totalCarsEntered; // ADDED: To automatically calculate how much the total cash amount should be
	private int totalMotorcyclesEntered; // ADDED: To automatically calculate how much the total cash amount should be

	/**
	 * Constructor of the class
	 */
	public ParkingStats(ParkingCash cash) {
		numberCars = 0;
		numberMotorcycles = 0;
		controlCars = new Object(); // Lock
		controlMotorcycles = new Object(); // Lock
		this.cash = cash;

		this.totalCarsEntered = 0; // ADDED: To automatically calculate how much the total cash amount should be
		this.totalMotorcyclesEntered = 0; // ADDED: To automatically calculate how much the total cash amount should be
	}

	public void carComeIn() {
		synchronized (controlCars) {
			numberCars++;
			this.totalCarsEntered++; // ADDED: To automatically calculate how much the total cash amount should be
		}
	}

	public void carGoOut() {
		/* MUTANT : "MSP (Modify Synchronized Block Parameter)" */
		synchronized (this) {
			numberCars--;
		}
		cash.vehiclePay();
	}

	public void motoComeIn() {
		synchronized (controlMotorcycles) {
			numberMotorcycles++;
			this.totalMotorcyclesEntered++; // ADDED: To automatically calculate how much the total cash amount should be
		}
	}

	public void motoGoOut() {
		synchronized (controlMotorcycles) {
			numberMotorcycles--;
		}
		cash.vehiclePay();
	}

	public synchronized long getNumberCars() {
		synchronized (controlCars) {
			return numberCars;
		}
	}

	public long getNumberMotorcycles() {
		synchronized (controlMotorcycles) {
			return numberMotorcycles;
		}
	}

	// ADDED: In order to get this value from the Main and Test functions
	public long getTotalCashAmount() {
		return this.cash.getTotalAmount();
	}

	// ADDED: To automatically calculate how much the total cash amount should be
	public int getTotalNumCarsEntered() {
		return this.totalCarsEntered;
	}

	// ADDED: To automatically calculate how much the total cash amount should be
	public int getTotalNumMotorcyclesEntered() {
		return this.totalMotorcyclesEntered;
	}
}