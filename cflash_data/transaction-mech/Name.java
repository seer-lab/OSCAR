// package csci4060u.lab7; // REMOVED

public class Name {
	
	private String title;
	
	private String firstName;
	
	private String middleName;
	
	private String lastName;
	
	private String suffix;
	
	public Name() {}
	
	public Name(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Name(String title, String firstName, String middleName,
			String lastName, String suffix) {
		this.title = title;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.suffix = suffix;
	}

	public String getTitle() {
		return title;
	}

	public Name setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public Name setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getMiddleName() {
		return middleName;
	}

	public Name setMiddleName(String middleName) {
		this.middleName = middleName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public Name setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getSuffix() {
		return suffix;
	}

	public Name setSuffix(String suffix) {
		this.suffix = suffix;
		return this;
	}
	
	@Override
	public String toString() {
		
		// Add the different parts of the name
		String[] parts = {title, firstName, middleName, lastName, suffix};
		String res = "";
		for (String p : parts) {
			if (p == null || p.isEmpty()) {
				continue;
			}
			res += p + " ";
		}
		
		return res.trim();
	}
}
