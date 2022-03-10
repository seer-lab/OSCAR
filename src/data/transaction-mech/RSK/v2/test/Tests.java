import static org.junit.Assert.*;
import org.junit.Test;

public class Tests {

    @Test
    public void testConcurrency() {
        
        int numAccounts = 20;
        int threads = numAccounts;
        int transactions = 1;
        double transactionAmount = 20;
        double accountsStartingBalance = 100;

        // Generate the info
		Client[] clients = generateClients(numAccounts);
		Account[] accounts = generateAccounts(numAccounts, clients, accountsStartingBalance);
		MECH mech = new MECH()
				.setClients(clients)
				.setAccounts(accounts);

        // Create and run threads
        TransactionSimulator[] sims = new TransactionSimulator[threads];
        for (int i = 0; i < threads; i ++) {
            sims[i] = new TransactionSimulator(mech, transactions, i, transactionAmount);
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

        // Print the transactions
		System.out.println();
        mech.print(System.out);
        System.out.println();
        
        for (Account account : accounts) {
            assertEquals(accountsStartingBalance, account.getBalance(), 0);
        }
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
	
	private static Account[] generateAccounts(int n, Client[] clients, double startingBalance) {
		
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

			// Create the account
			accounts[i] = new Account()
					.setAccountID(i)
					.setBalance(startingBalance)
					.setOwners(owners);
		}
		
		return accounts;
	}
}