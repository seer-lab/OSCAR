// package csci4060u.lab7; // REMOVED

import org.junit.Test;

public class TestMECH {

	@Test
	public void testParallelTransactions() {

		TestMECHRunner r1 = new TestMECHRunner();
		TestMECHRunner r2 = new TestMECHRunner();
		MECH mech = new MECH();
		
		// Create test accounts
		Account a1 = new Account()
				.setAccountID(1)
				.setBalance(1000000000);
		Account a2 = new Account()
				.setAccountID(2)
				.setBalance(1000000000);
		mech.addAccount(a1).addAccount(a2);
		r1.mech = mech;
		r2.mech = mech;
		
		// Create transactions
		int n = 1000000;
		double amount = 0.001f;
		Transaction[] t1s = new Transaction[n];
		Transaction[] t2s = new Transaction[n];
		for (int i = 0; i < n; i ++) {
			t1s[i] = new Transaction(a1, a2, amount);
			t2s[i] = new Transaction(a2, a1, amount);
		}
		r1.transactions = t1s;
		r2.transactions = t2s;
		
		// Run them to check for deadlocks
		r1.start();
		r2.start();
	}

	private class TestMECHRunner extends Thread {
		
		private Transaction[] transactions;
		
		private MECH mech;
		
		public TestMECHRunner() {
			this.transactions = new Transaction[0];
		}
		
		@Override
		public void run() {
			for (Transaction t : transactions) {
				mech.run(t);
			}
		}
	}
}
