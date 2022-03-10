// package csci4060u.lab7; // REMOVED

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

public class MECH {

	private Account[] accounts;
	
	private Client[] clients;
	
	private List<Transaction> transactions;
	
	public MECH() {
		this.transactions = new ArrayList<>();
	}
	
	/**
	 * Attempts to execute the specified transaction. If the transaction is
	 * null or has already been executed, the system will not execute the
	 * transaction.
	 * 
	 * @param t	the transaction.
	 * @return true if and only if the transaction was executed.
	 * 
	 * @see Transaction#execute()
	 * @see Transaction#getStatus()
	 */

	public boolean run(Transaction t) {
		
		// Nothing to do
		if (t == null || t.getStatus() != TransactionStatus.NOT_INITIATED) {
			return false;
		}
		
		// Execute the transaction and add it to the log
		t.execute();
		log(t);
		
		return true;
	}
	
	/* MUTANT : "RSK (Removed Synchronized Keyword)" */
	private void log(Transaction t) {
		this.transactions.add(t);
	}
	
	/**
	 * Prints the transactions and accounts logged by the system.
	 * 
	 * @param ps	the print stream to print to.
	 */
	public void print(PrintStream ps) {
		
		if (ps == null || transactions == null) {
			return;
		}

		// ADDED: Sort list of transactions for better understanding of what's going on...
		Collections.sort(transactions, new Comparator<Transaction>() {
			@Override
			public int compare(Transaction t1, Transaction t2) {
				return ((int)t1.getFrom().getAccountID()) - ((int)t2.getFrom().getAccountID());
			}
		});
		
		// Print all the transactions
		ps.println("Transactions");
		ps.println("============");
		for (Transaction t : transactions) {
			Account from = t.getFrom();
			Account to = t.getTo();
			double amount = t.getAmount();
			TransactionStatus s = t.getStatus();
			ps.printf("From: %5d | To: %5d | Amount: $%.2f | Status: %s\n",
					from.getAccountID(), to.getAccountID(), amount, s.name());
		}
		
		// Print all the accounts
		ps.println();
		ps.println("Accounts");
		ps.println("============");
		if (accounts != null) {
			for (Account a : accounts) {
				long id = a.getAccountID();
				Client[] owners = a.getOwners();
				if (owners == null) {
					owners = new Client[0];
				}
				double balance = a.getBalance();
				ps.printf("Account Number: %5d | Balance: $%.2f\n", id, balance);
				ps.println("Owners:");
				if (owners.length == 0) {
					ps.println(" None.");
				}
				for (Client o : owners) {
					Name name = o.getName();
					ps.printf(" %s (%d)\n", name.toString(), o.getClientID());
				}
			}
		}
	}

	public Account[] getAccounts() {
		return accounts;
	}
	
	public MECH setAccounts(Account[] accounts) {

		if (accounts == null) {
			accounts = new Account[0];
		}
		this.accounts = accounts;
		return this;		
	}
	
	public MECH addAccount(Account account) {

		if (account == null) {
			return this;
		}
		
		// Expand the array and add the value
		if (accounts == null) {
			this.accounts = new Account[0];
		}
		int n = accounts.length;
		Account[] tmp = new Account[n + 1];
		for (int i = 0; i < n; i ++) {
			tmp[i] = accounts[i];
		}
		tmp[n] = account;
		this.accounts = tmp;		
		return this;
	}

	public Client[] getClients() {
		return clients;
	}

	public MECH setClients(Client[] clients) {
		if (clients == null) {
			clients = new Client[0];
		}
		this.clients = clients;
		return this;
	}
	
	public MECH addClient(Client client) {
		
		if (client == null) {
			return this;
		}
		
		// Expand the array and add the new client
		if (clients == null) {
			this.clients = new Client[0];
		}
		int n = clients.length;
		Client[] tmp = new Client[n + 1];
		for (int i = 0; i < n; i ++) {
			tmp[i] = clients[i];
		}
		tmp[n] = client;
		this.clients = tmp;
		
		return this;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}
	
	public int getTransactionCount() {
		return transactions == null? 0 : transactions.size();
	}
}
