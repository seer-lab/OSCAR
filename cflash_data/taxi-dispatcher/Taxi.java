// drops customers off at locations
public class Taxi implements Runnable {

    private Dispatcher myDispatcher;

    // a taxi's customer that will be dropped off at a location
    private Customer myCustomer;
    
    // location of the taxi
    int location;

    // ADDED: To keep track of the number of customers each taxi picked up and droped off
    int numCustomersDropped;

    // constructor for taxi thread
    public Taxi (Dispatcher dispatch) {

        // initialize taxi's base
        this.myDispatcher = dispatch;

        // taxi starts off at the taxi garage
        this.location = 0;

        // ADDED: To keep track of the number of customers each taxi picked up and droped off
        this.numCustomersDropped = 0;
    }

    // function that will have a taxi go to a locaton
    private void gotoLocation (int dest) {

        try {

            // calculate distance to destination
            int deltaLocation = Math.abs(dest - location);
            
            // go to destination
            Thread.sleep(deltaLocation);
        
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // update taxi location
        this.location = dest;
    }

    public void run() {

        // check if dispatcher has customers
        while (!this.myDispatcher.checkCustomers()) {

            // get a customer from dispatch
            this.myCustomer = myDispatcher.dispatchResp();

            // ADDED: try-catch block, as a result of constant "NullPointerException"s
            try {
                // get taxi to go to customer location
                gotoLocation (this.myCustomer.getLocation());
                
                // CHANGED: Added class and method printing from
                System.out.printf ("[Taxi: run()] %s arrived at customer %d location!\n", Thread.currentThread().getName(), this.myCustomer.getId());
                
                // deliver customer to their destination
                gotoLocation (this.myCustomer.getDestination());
                
                // CHANGED: Added class and method printing from
                System.out.printf ("[Taxi: run()] %s delivered customer %d to their destination (at %d km)!\n", Thread.currentThread().getName(), this.myCustomer.getId(), this.myCustomer.getDestination());

                // ADDED: To keep track of the number of customers each taxi picked up and droped off
                this.numCustomersDropped++;
            } catch (NullPointerException e) {}
        }

        // return to dispatch once the day is done
        gotoLocation (0);
    
        System.out.printf ("[Taxi: run()] %s has returned to garage\n", Thread.currentThread().getName());

    }
}