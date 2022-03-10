// package csci4060u.lab7; // REMOVED

public class TransactionSimulator extends Thread {
	
	private MECH mech;
	
	private int maxTransactions;

	// ADDED: The index to the current account, as well as the amount to be transferred, will be provided to the thread via the constructor
	private int currentAccountIndex;
	private double amount;

	// public TransactionSimulator(MECH mech, int maxTransactions) { // CHANGED: The index to the current account, as well as the amount to be transferred, will be provided to the thread via the constructor
	public TransactionSimulator(MECH mech, int maxTransactions, int index, double amount) {
		this.mech = mech;
		this.maxTransactions = maxTransactions;

		// ADDED: The index to the current account, as well as the amount to be transferred, will be provided to the thread via the constructor
		this.currentAccountIndex = index;
		this.amount = amount;
	}
	
	@Override
	public void run() {
		
		// No mech or transactions
		if (mech == null || maxTransactions < 1) {
			return;
		}
		
		// Get and check the accounts
		Account[] accounts = mech.getAccounts();
		if (accounts == null || accounts.length == 0) {
			return;
		}

		// REMOVED: No longer needed, since the transaction amount is no longer being randomized
		// int n = accounts.length;
		// double min = 5, max = 117;
		
		// Keep randomly generating transactions
		// while (mech.getTransactionCount() < maxTransactions) { // CHANGED: This may be a bug. I believe the behaviour wanted here is to generate as many transactions as specified. However, this approach causes some of the threads not to finish if the number of transactions to run is lower than the number of threads
		for (int i = 0; i < maxTransactions; i++) {

			// ADDED: The to/from account, as well as the transaction amouts, will be set beforehand as opposed to randomizing them
			Account from = accounts[this.currentAccountIndex];
			Account to = accounts[(this.currentAccountIndex+1)%accounts.length];

			// REMOVE: The to/from account, as well as the transaction amouts, will be set beforehand as opposed to randomizing them
			// // Generate the transaction
			// Account from = accounts[(int) (Math.random() * n)];
			// Account to = accounts[(int) (Math.random() * n)];
			// double amount = Math.random() * (max - min) + min;
			Transaction t = new Transaction(from, to, amount);
			
			// Run the transaction
			this.mech.run(t);
		}
	}
}
