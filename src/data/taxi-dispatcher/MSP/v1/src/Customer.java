// Busy customers with places to be
public class Customer {

    private int customerId;
    private int location;
    private int destination;

    public Customer (int id, int loc, int dest) {
    
        this.customerId = id;
        this.location = loc;
        this.destination = dest;
    
    }

    // getter functions
    public int getId() {
        return this.customerId;
    }

    public int getLocation() {
        return this.location;
    }

    public int getDestination() {
        return this.destination;
    }

}