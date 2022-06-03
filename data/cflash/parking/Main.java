/**
 * Taken from: "Java 9 Concurrency Cookbook - Second Edition"
 */

public class Main {

	public static void main(String[] args) {

		// ADDED: Parameter to the Sensor (Runnable) class. Extracted into a variable for clarity
		int numChecksPerSensor = 10;

		// ADDED: Parking cost is given from here in order to automatically calculate total cash amount in tests
		int parkingCost = 2;

		int numCarsToEnter = 2; // ADDED: Number of cars to enter per sensor is given here in order to automatically calculate total cash amount in tests
		int numMotosToEnter = 1; // ADDED: Number of motos to enter per sensor is given here in order to automatically calculate total cash amount in tests

		// ParkingCash cash = new ParkingCash(); // CHANGED: Parking cost is given from here in order to automatically calculate total cash amount in tests
		ParkingCash cash = new ParkingCash(parkingCost);
		ParkingStats stats = new ParkingStats(cash);

		System.out.printf("Parking Simulator\n"); // REMOVED
		int numberSensors = 2 * Runtime.getRuntime().availableProcessors();
		Thread threads[] = new Thread[numberSensors];
		
		for (int i = 0; i < numberSensors; i++) {
			// Sensor sensor=new Sensor(stats); // CHANGED: Added parameter to threaded
			// class instead of set from inside, so that it can be altered from the tests
			Sensor sensor = new Sensor(stats, numChecksPerSensor, numCarsToEnter, numMotosToEnter);
			Thread thread = new Thread(sensor);
			thread.start();
			threads[i] = thread;
		}

		for (int i = 0; i < numberSensors; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.printf("Number of cars: %d\n", stats.getNumberCars());
		System.out.printf("Number of motorcycles: %d\n", stats.getNumberMotorcycles());

		cash.close();

		// ADDED: Moved to ParkingCash
		System.out.printf("The total cash ammount is: $%d\n", stats.getTotalCashAmount());

		// DEBUGGING/TESTING
		System.out.println(parkingCost*numberSensors*numChecksPerSensor*(numCarsToEnter+numMotosToEnter));
	}
}
