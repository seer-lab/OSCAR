// package csci4060u.lab7; // REMOVED

import static org.junit.Assert.*;

import org.junit.Test;

public class TestAccount {

	@Test
	public void testFields() {
		
		// Basic field test
		long id = 716281612121L;
		double balance = 788811;
		Account a = new Account()
				.setAccountID(id)
				.setBalance(balance);
		assertEquals(id, a.getAccountID());
		assertEquals(balance, a.getBalance(), 0);
	}
	
	@Test
	public void testOwners() {
		
		// Test with no owners
		Account a = new Account();
		assertNotNull(a.getOwners());
		assertEquals(0, a.getOwners().length);
		
		// Test with some owners
		long id = 1;
		Client[] owners = {
				new Client().setClientID(id++),
				new Client().setClientID(id++),
				new Client().setClientID(id++),
				new Client().setClientID(id++)};
		a.setOwners(owners);
		assertArrayEquals(owners, a.getOwners());
		
		// Test adding owners
		a.setOwners(null);
		assertNotNull(a.getOwners());
		assertEquals(0, a.getOwners().length);
		a.addOwner(owners[0]);
		assertEquals(1, a.getOwners().length);
		assertEquals(owners[0], a.getOwners()[0]);
		a.addOwner(null); // should not add a null owner
		assertEquals(1, a.getOwners().length);
	}
	
	@Test
	public void testBalance() {
		
		double balance = 500;
		double delta = 10;
		Account a = new Account()
				.setBalance(balance);
		
		// Try adding an amount
		assertEquals(balance, a.getBalance(), 0);
		a.updateBalance(delta);
		assertEquals(balance + delta, a.getBalance(), 0);
		
		// Try subtracting an amount
		a.setBalance(balance);
		a.updateBalance(-delta);
		assertEquals(balance - delta, a.getBalance(), 0);
	}
}
