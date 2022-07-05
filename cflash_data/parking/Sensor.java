/**
 *  Taken from: "Java 9 Concurrency Cookbook - Second Edition"
 */

import java.util.concurrent.TimeUnit;

public class Sensor implements Runnable {

	private ParkingStats stats;
	private int numChecks;

	private int numCarsToEnter; // ADDED: Number of cars to enter per sensor is given in constructor in order to automatically calculate total cash amount in tests
	private int numMotosToEnter; // ADDED: Number of motos to enter per sensor is given in constructor in order to automatically calculate total cash amount in tests

	// CHANGED: Added parameters to threaded class instead of set from inside, so that it can be altered from the tests
	// public Sensor(ParkingStats stats) {
	public Sensor(ParkingStats stats, int numChecks, int numCarsToEnter, int numMotosToEnter) {
		this.stats = stats;
		this.numChecks = numChecks; // ADDED
		this.numCarsToEnter = numCarsToEnter; // ADDED: See details above
		this.numMotosToEnter = numMotosToEnter; // ADDED: See details above
	}

	@Override
	public void run() {

		// for (int i = 0; i < 10; i++) { // CHANGED: See description above
		for (int i = 0; i < this.numChecks; i++) {

			// ADDED: Number of cars to enter per sensor is given in constructor in order to automatically calculate total cash amount in tests
			for (int j = 0; j < this.numCarsToEnter; j++) {
				stats.carComeIn();
			}

			// stats.carComeIn(); // REMOVED: Moved into loop above
			// stats.carComeIn(); // REMOVED: Moved into loop above

			// REMOVED: More noising is not needed
			// try {
			// TimeUnit.MILLISECONDS.sleep(50);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }

			// ADDED: Number of motos to enter per sensor is given in constructor in order to automatically calculate total cash amount in tests
			for (int j = 0; j < this.numMotosToEnter; j++) {
				stats.motoComeIn();
			}

			// stats.motoComeIn(); // REMOVED: Moved into loop above

			// REMOVED: More noising is not needed
			// try {
			// TimeUnit.MILLISECONDS.sleep(50);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }

			// ADDED: Number of motos to enter per sensor is given in constructor in order to automatically calculate total cash amount in tests
			for (int j = 0; j < this.numMotosToEnter; j++) {
				stats.motoGoOut();
			}

			// stats.motoGoOut(); // REMOVED: Moved into loop above

			// ADDED: Number of cars to enter per sensor is given in constructor in order to automatically calculate total cash amount in tests
			for (int j = 0; j < this.numCarsToEnter; j++) {
				stats.carGoOut();
			}

			// stats.carGoOut(); // REMOVED: Moved into loop above
			// stats.carGoOut(); // REMOVED: Moved into loop above
		}
	}

}
