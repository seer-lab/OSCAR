// package csci4060u.lab7; // REMOVED

import static org.junit.Assert.*;

import org.junit.Test;

public class TestClient {

	@Test
	public void testFields() {
		
		long id = 1002031;
		Name name = new Name()
				.setFirstName("Katherine")
				.setLastName("Halsey");
		Client c = new Client()
				.setClientID(id)
				.setName(name);
		assertEquals(id, c.getClientID());
		assertEquals(name, c.getName());
	}

	@Test
	public void testEquals() {
		
		// Clients with the same ID should be equal
		long id1 = 1921, id2 = 4242421;
		assertEquals(new Client().setClientID(id1),
				new Client().setClientID(id1));
		
		// Different IDs should not be equal
		assertNotEquals(new Client().setClientID(id1),
				new Client().setClientID(id2));
	}
}
