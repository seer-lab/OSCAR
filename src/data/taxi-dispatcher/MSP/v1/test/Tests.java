import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

public class Tests {

    int numCustomers;
    int maxKmRange;
    int numTaxis;

    Dispatcher taxiHQ;

    Thread[] threads;
    Taxi[] taxis;

	@Before
	public void initializeTests() {

        this.maxKmRange = 1500;
        this.numTaxis = Runtime.getRuntime().availableProcessors() * 5;
        this.numCustomers = numTaxis * 3;

        this.taxiHQ = new Dispatcher (numCustomers, maxKmRange);

        this.threads = new Thread [numTaxis];
        this.taxis = new Taxi [numTaxis];
	}

    @Test
	public void testCostumerCount() {
        
        // Create and start threads (taxis)
        for (int i = 0; i < this.numTaxis; i ++) {

            Taxi taxi = new Taxi(this.taxiHQ);
            this.taxis[i] = taxi;

            this.threads[i] = new Thread(taxi, "Taxi " + i);
            this.threads[i].start();
        }

        // Wait for all threads to finish executing and sum up the customers dropped off by each thread
        int totalNumCustomersDropped = 0;
        for (int i = 0; i < numTaxis; i ++) {
            try {
                this.threads[i].join();
                totalNumCustomersDropped += this.taxis[i].numCustomersDropped;
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println();
        assertEquals(numCustomers, totalNumCustomersDropped);
    }
}