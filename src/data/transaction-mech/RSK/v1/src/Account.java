// package csci4060u.lab7; // REMOVED

public class Account {
	
	private long accountID;
	
	private double balance;
	
	private Client[] owners;
	
	public Account() {
		this(0, 0.0, null);
	}
	
	public Account(long accountID, double balance) {
		this(accountID, balance, null);
	}
	
	public Account(long accountID, double balance, Client[] owners) {
		this.accountID = accountID;
		this.balance = balance;
		setOwners(owners);
	}

	public long getAccountID() {
		return accountID;
	}

	public Account setAccountID(long accountID) {
		this.accountID = accountID;
		return this;
	}

	public double getBalance() {
		return balance;
	}

	public Account setBalance(double balance) {
		this.balance = balance;
		return this;
	}

	public Client[] getOwners() {
		return owners;
	}

	public Account setOwners(Client[] owners) {
		if (owners == null) {
			owners = new Client[0];
		}
		this.owners = owners;
		return this;
	}
	
	public Account addOwner(Client owner) {

		if (owner == null) {
			return this;
		}
		
		// Expand the array and add the new client
		int n = owners.length;
		Client[] tmp = new Client[n + 1];
		for (int i = 0; i < n; i ++) {
			tmp[i] = owners[i];
		}
		tmp[n] = owner;
		this.owners = tmp;
		
		return this;
	}
	
	/* MUTANT : "RSK (Removed Synchronized Keyword)" */
	public void updateBalance(double amount) {
		this.balance += amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (accountID ^ (accountID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (accountID != other.accountID)
			return false;
		return true;
			
	}
}
