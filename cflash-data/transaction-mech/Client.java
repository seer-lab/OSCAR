// package csci4060u.lab7; // REMOVED

public class Client {
	
	protected long clientID;
	
	protected Name name;

	public long getClientID() {
		return clientID;
	}

	public Client setClientID(long clientID) {
		this.clientID = clientID;
		return this;
	}

	public Name getName() {
		return name;
	}

	public Client setName(Name name) {
		this.name = name;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (clientID ^ (clientID >>> 32));
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
		Client other = (Client) obj;
		if (clientID != other.clientID)
			return false;
		return true;
	}
}
