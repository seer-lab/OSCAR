// package csci4060u.lab7; // REMOVED

import static org.junit.Assert.*;

import org.junit.Test;

public class TestName {

	@Test
	public void testName() {
		
		// Test the getters and setters
		String fn = "John", in = "A.", ln = "Smith";
		String title = "Mr", suffix = "III";
		Name name = new Name()
				.setFirstName(fn)
				.setLastName(ln)
				.setMiddleName(in)
				.setTitle(title)
				.setSuffix(suffix);
		assertEquals(fn, name.getFirstName());
		assertEquals(ln, name.getLastName());
		assertEquals(in, name.getMiddleName());
		assertEquals(title, name.getTitle());
		assertEquals(suffix, name.getSuffix());
		
		// Test the formatting
		assertEquals(title + " " + fn + " " + in + " " + ln + " " + suffix,
				name.toString());
		name.setMiddleName(null);
		assertEquals(title + " " + fn + " " + ln + " " + suffix,
				name.toString());
	}

}
