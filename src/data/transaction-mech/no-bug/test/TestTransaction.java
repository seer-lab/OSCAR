// package csci4060u.lab7; // REMOVED

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTransaction {

	@Test
	public void testTransaction() {
		
		Account from = getRandom(), to = getRandom();
		double amount = 10;
		Transaction t = new Transaction(from, to, amount);
		assertEquals(from, t.getFrom());
		assertEquals(to, t.getTo());
		assertEquals(amount, t.getAmount(), 0);
		assertEquals(TransactionStatus.NOT_INITIATED,
				t.getStatus());
		
		double fs = from.getBalance();
		double ts = to.getBalance();
		t.execute();
		assertEquals(fs - amount, from.getBalance(), 0.00001);
		assertEquals(ts + amount, to.getBalance(), 0.00001);
	}
	
	private Account getRandom() {
		return new Account()
				.setAccountID((int) (Math.random() * 1000))
				.setBalance(Math.random() * 10000 + 100)
				.addOwner(new Client().setClientID((int) (Math.random() * 1000)));
	}

}
