// package csci4060u.lab7; // REMOVED

public class Transaction {
	
	private Account from;
	
	private Account to;
	
	private double amount;
	
	private TransactionStatus status;
	
	public Transaction() {
		this(null, null, 0);
	}
	
	public Transaction(Account from, Account to, double amount) {
		this.status = TransactionStatus.NOT_INITIATED;
		this.from = from;
		this.to = to;
		this.amount = Math.abs(amount);
	}
	
	public Transaction execute() {

		Account firstLock;
		Account secondLock;

		if (to.getAccountID() > from.getAccountID()) {
			firstLock = from;
			secondLock = to;
		} else {
			firstLock = to;
			secondLock = from;
		}

				// Don't do anything if the transaction was completed
		if (status == TransactionStatus.COMPLETE) {
			return this;
		}
		
		// Check arguments
		if (from == null || to == null || from.equals(to)) {
			this.status = TransactionStatus.INVALID;
			return this;
		} if (amount == 0) {
			this.status = TransactionStatus.COMPLETE;
			return this;
		} if (from.getBalance() < amount) { // not enough funds
			this.status = TransactionStatus.MISSING_FUNDS;
			return this;
		}
		
		// Update the status
		this.status = TransactionStatus.WAITING;
		
		// Complete the transaction
		this.from.updateBalance(-amount);
		this.to.updateBalance(amount);
		this.status = TransactionStatus.COMPLETE;
		
		return this;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public Account getFrom() {
		return from;
	}

	public Account getTo() {
		return to;
	}

	public double getAmount() {
		return amount;
	}
}
