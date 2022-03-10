// package csci4060u.lab7; // REMOVED

public class Main {
	
	// REMOVED: Not used
	// private static final int DEFAULT_THREAD_COUNT = 10;
	
	// private static final int DEFAULT_ACCOUNT_COUNT = 100;
	
	// private static final int DEFAULT_TRANSACTION_COUNT = 1000;
	
	public static void main(String[] args) {
		
		System.out.println("Usage: <program_name> <#_of_accounts> <#_of_threads> <#_of_transaction>");
		
		// REMOVED: Number of accounts to create will be set in the program, not externally by the user
		// // Parse the number of accounts
		// int numaccounts = DEFAULT_ACCOUNT_COUNT;
		// if (args.length > 0) {
		// 	try {
		// 		numaccounts = Integer.parseInt(args[0]);
		// 	} catch (NumberFormatException e) {
		// 		System.err.println("Invalid number of accounts.");
		// 		System.exit(1);
		// 	}
		// 	if (numaccounts < 2) {
		// 		System.err.println("There must be at least two accounts.");
		// 		System.exit(1);
		// 	}
		// }
		
		// ADDED: Number of accounts to create will be set in the program, not externally by the user
		int numaccounts = 20;
		
		// REMOVED: The number of threads will be calculated depending on the number of accounts to create
		// // Parse the number of threads
		// int threads = DEFAULT_THREAD_COUNT;
		// if (args.length > 1) {
		// 	try {
		// 		threads = Integer.parseInt(args[1]);
		// 	} catch (NumberFormatException e) {
		// 		System.err.println("Invalid number of threads.");
		// 		System.exit(1);
		// 	}
		// 	if (threads < 1) {
		// 		System.err.println("There must be at least one thread.");
		// 		System.exit(1);
		// 	}
		// }
		
		// ADDED: The number of threads will be calculated depending on the number of accounts to create
		int threads = numaccounts;
		
		// REMOVED: Number of transactions to perform will be set in the program, not externally by the user
		// // Parse the number of transactions
		// int transactions = DEFAULT_TRANSACTION_COUNT;
		// if (args.length > 2) {
		// 	try {
		// 		transactions = Integer.parseInt(args[2]);
		// 	} catch (NumberFormatException e) {
		// 		System.err.println("Invalid number of transactions.");
		// 		System.exit(1);
		// 	}
		// 	if (transactions < 1) {
		// 		System.err.println("There must be at least one transactions.");
		// 		System.exit(1);
		// 	}
		// }

		// ADDED: Number of transactions to perform will be set in the program, not externally by the user
		int transactions = 1;
		
		// Generate the info
		Client[] clients = generateClients(numaccounts);
		Account[] accounts = generateAccounts(numaccounts, clients);
		MECH mech = new MECH()
				.setClients(clients)
				.setAccounts(accounts);
		
		// ADDED: The amount to be transferred will be set beforehand and  provided to the thread via the constructor - see below
		double transactionAmount = 20;
		
		// Create the simulators
		TransactionSimulator[] sims = new TransactionSimulator[threads];
		for (int i = 0; i < threads; i ++) {
			// sims[i] = new TransactionSimulator(mech, transactions); // CHANGED: The index to the current account, as well as the amount to be transferred, will be provided to the thread via the constructor
			sims[i] = new TransactionSimulator(mech, transactions, i, transactionAmount);
		}
		
		// Print some info
		System.out.println();
		System.out.println("Bank Transaction System");
		System.out.println("=======================");
		System.out.println("Number of accounts: " + numaccounts);
		System.out.println("Thread count: " + threads);
		System.out.println("Transactions to execute: " + transactions);
		
		// Start the threads
		long t = System.currentTimeMillis();
		for (int i = 0; i < threads; i ++) {
			sims[i].start();
		}
		
		// Join all the threads
		for (int i = 0; i < threads; i ++) {
			try {
				sims[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		t = System.currentTimeMillis() - t;
		
		// Print the transactions
		System.out.println();
		mech.print(System.out);

		// ADDED: Own printing of current accounts' status
		// for (Account account : accounts) {
		// 	System.out.println("(" + account.getAccountID() + ") " + account.getBalance());
		// }
		// System.out.println();
		
		System.out.println("Done execution in " + t + "ms.");
	}
	
	private static Client[] generateClients(int n) {
		
		// Nothing to do
		if (n < 1) {
			return new Client[0];
		}
		
		String[] fnames = {"Bob", "Sheri", "John", "Hannah", "Jake", "Sara"};
		String[] lnames = {"Smith", "Doe", "McCarthy", "Wang", "Templin", "Faler"};
		String[] initials = {"P.", "A.", "C.", "K.", "E.", "D."};
		
		Client[] clients = new Client[n];
		for (int i = 0; i < n; i ++) {
			
			// Build the name
			String fn = fnames[(int) (Math.random() * fnames.length)];
			String ln = lnames[(int) (Math.random() * lnames.length)];
			String in = initials[(int) (Math.random() * initials.length)];
			Name name = new Name()
					.setFirstName(fn)
					.setLastName(ln)
					.setMiddleName(in);
			
			// Create the client
			Client c = new Client();
			c.setClientID(i).setName(name);
			clients[i] = c;
		}
		
		return clients;
	}
	
	private static Account[] generateAccounts(int n, Client[] clients) {
		
		// Nothing to do
		if (n < 1) {
			return new Account[0];
		}
		
		double min = 100, max = 10000;
		
		// Generate the accounts
		Account[] accounts = new Account[n];
		for (int i = 0; i < n; i ++) {
			
			// Determine number of owners
			Client[] owners = null;
			if (clients != null && clients.length > 1) {
				int oc = Math.random() < 0.5? 1 : 2;
				owners = new Client[oc];
				owners[0] = clients[(int) (Math.random() * clients.length)];
				while (oc > 1) {
					Client c = clients[(int) (Math.random() * clients.length)];
					if (c != owners[0]) {
						owners[1] = c;
						break;
					}
				}
			} else if (clients != null && clients.length == 1) {
				owners = new Client[1];
				owners[0] = clients[0];
			}
			
			// ADDED: Balance is set the same for all accounts, as opposed to being randomized
			double startingBalance = 100;
			// Create the account
			accounts[i] = new Account()
					.setAccountID(i)
					// .setBalance(Math.random() * (max - min) + min) // CHANGED: Balance is set the same for all accounts, as opposed to being randomized
					.setBalance(startingBalance)
					.setOwners(owners);
		}
		
		return accounts;
	}
}
