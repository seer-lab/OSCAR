import java.util.ArrayList;
import java.util.Random;
import java.lang.*;
// import java.util.concurrent.locks.ReentrantLock;

public class lab7 {

    public static void main (String [] args) {

        // ADDED: Storing the customer count in a variable for better readibility and testing
        int numCustomers = 100;

        // ADDED: Storing the maximum range (km) for taxis to travel in a variable for better readibility and testing
        int maxKmRange = 1500;

        // initialize 5 taxis (threads)
        final int numTaxis = 50;

        // ADDED: Print general run information
        System.out.println("----------------------");
        System.out.println("DISPATCHER INFORMATION");
        System.out.println("----------------------");
        System.out.printf("Taxis available: %d\n", numTaxis);
        System.out.printf("Customers: %d\n", numCustomers);
        System.out.println("----------------------\n");

        // initialize dispatcher location and customers
        // Dispatcher taxiHQ = new Dispatcher (10, 1500); // CHANGED: Storing the customer count and the maximum range in a variable for better readibility and testing
        Dispatcher taxiHQ = new Dispatcher (numCustomers, maxKmRange);

        // CHANGED: In the next few lines, I renamed the "taxis" array to "threads", and created a new "taxis" array where the Runnable objects are stored
        Thread[] threads = new Thread [numTaxis];
        Taxi[] taxis = new Taxi [numTaxis];
        
        // start up taxis for the work day
        for (int i = 0; i < numTaxis; i ++) {
            
            // CHANGED: Also storing Runnable objects in an array
            Taxi taxi = new Taxi(taxiHQ);
            taxis[i] = taxi;

            threads[i] = new Thread(taxi, "Taxi " + i);
            threads[i].start(); // ADDED: Now starting the threads in the same loop in which they are created
        }

        // REMOVED: Now starting the threads in the same loop in which they are created
        // for (int i = 0; i < numTaxis; i ++) {
        //     threads[i].start();
        // }

        // ADDED: Count the number of customers that each taxi picked up and dropped off. It should match the initial number of customers
        int totalNumCustomersDropped = 0;

        // ADDED: Wait for all threads to finish executing
        for (int i = 0; i < numTaxis; i ++) {
            try {
                threads[i].join();
                System.out.println("Taxi " + i + ": " + taxis[i].numCustomersDropped + " customers");
                totalNumCustomersDropped += taxis[i].numCustomersDropped;
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        // ADDED
        System.out.println();
        System.out.printf("[Main] All taxis have returned to the dispatcher garage\n%d customers were picked up and dropped off today\n", totalNumCustomersDropped);
    }
}