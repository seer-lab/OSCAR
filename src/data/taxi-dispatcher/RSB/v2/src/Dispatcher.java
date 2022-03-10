import java.util.ArrayList;
import java.util.Random;
import java.lang.*;

// generates customers for taxis to drop off
// calls taxis back to garage at the end of the day
public class Dispatcher {

    private static ArrayList <Customer> customers = new ArrayList <>();
    
    // REMOVED: No longer used - instead, using synchronized blocks
    // // create java locks for threads
    // private final ReentrantLock customerLock = new ReentrantLock();
    // private final ReentrantLock checkLock = new ReentrantLock();
    
    // generates customers to get picked up and dropped off within a range
    public Dispatcher (int numCustomers, int range) {

        Random rand = new Random();

        // generate random customers
        for (int i = 0; i < numCustomers; i ++) {
            
            int locationDist = Math.abs(rand.nextInt() % range) + 1;
            int destinationDist = Math.abs(rand.nextInt() % range) + 1;
            
            // add customers to list of customers
            customers.add (new Customer (i + 1, locationDist, destinationDist));
        }

        // ADDED: Log printing
        System.out.printf("[Dispatcher: Dispatcher()] Generated %d customers\n", customers.size());
    }

    public Customer dispatchResp() {

        Customer assignedCustomer = new Customer (0, 0, 0);

        // lock customer list to prent race condition
        // customerLock.lock(); // REMOVED: Replaced with a synchronized block

        // ADDED: Synchronized block instead of lock objects
        synchronized (customers) {
            // get customer from list
            assignedCustomer = customers.remove(0);
        }
        // customerLock.unlock(); // REMOVED: Replaced with a synchronized block

        // ADDED: try-catch block as a result of constant "NullPointerException"s
        try {
            // CHANGED: Added class and method printing from
            System.out.println ("[Dispatcher: dispatchResp()] " + Thread.currentThread().getName() + " will get customer " + 
            assignedCustomer.getId() + " (" + assignedCustomer.getLocation() + " km away)");
        } catch(NullPointerException e) {}
        
        return assignedCustomer;
    }

    // check for available customers
    public boolean checkCustomers() {

        boolean check;
        // checkLock.lock(); // REMOVED: Replaced with a synchronized block

        // ADDED: Synchronized block instead of lock objects
        /* MUTANT : "RSB (Removed Synchronized Block)" */
        check = customers.isEmpty();
        
        // checkLock.unlock(); // REMOVED: Replaced with a synchronized block
        
        return check;
    
    }
}