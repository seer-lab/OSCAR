/**
 * Taken from: "Java 9 Concurrency Cookbook - Second Edition"
 */

// package com.packtpub.java9.concurrency.cookbook.chapter02.recipe01.parking;

public class ParkingCash {

	// private static final int cost = 2;
	private int cost; // CHANGED: In order to automatically calculate final value from tests

	private long cash;
	private long totalAmmount; // ADDED: Moved here from close() function

	public ParkingCash(int cost) {
		this.cash = 0;
		this.totalAmmount = 0; // ADDED: Moved here from close() function
		this.cost = cost; // ADDED: In order to automatically calculate final value from tests
	}

	public synchronized void vehiclePay() {
		this.cash += this.cost;
	}

	public void close() {

		System.out.printf("Closing accounting\n");
		// long totalAmmount; // REMOVED: Moved as a global class variable instead
		/* MUTANT : "RSB (Removed Synchronized Block)" */
		this.totalAmmount = this.cash;
		this.cash = 0;
		System.out.printf("The total cash ammount is: $%d\n",totalAmmount); //
		// REMOVED: Moved to the main function instead
	}

	// ADDED: In order to get this value from the Main and Test functions
	// Get totalCashAmount. Only non-zero after closing.
	public long getTotalAmount() {
		return this.totalAmmount;
	}
}