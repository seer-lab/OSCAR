/*
    Banking system where threads make deposits/withdrawls of random amounts of money from an account.
    
    Some print statements out of order, tried to include them in the thread execution but that caused threads to execute in sequence due to buffered nature of System.out.println(). So occasionally the last few transactions will be printed out of order, the true final transaction will have a matching balance with the final balance.
*/

// import java.util.concurrent.ThreadLocalRandom; // REMOVED: Commented out, as it's not used anymore

public class Bank {

    // public static String main (String [] args) { // CHANGED: The main method must return void
    public static void main (String [] args) {
        
        // CHANGED: Using a value for the initial balance, as opposed to user input
        // int balance = Integer.parseInt (args [0]);
        // if (balance < 0) {
        //     String message = "Error: Initial balance should be positive.";
        //     System.out.println (message);
        //     // return message; // REMOVED: The main method must return void
        // }
        int balance = 1000;
        
        // CHANGED: Using a value for the initial balance, as opposed to user input
        // int numThreads = Integer.parseInt (args [1]);
        // if (numThreads < 2) {
        //     String message = "Error: Should have at least 2 threads.";
        //     System.out.println (message);
        //     // return message; // REMOVED: The main method must return void
        // }
        int numThreads = 5;
        
        Account account = new Account(balance);
        // Thread threads [] = new Thread [numThreads]; // CHANGED: Array of Thread to array of BankThread
        BankThread threads [] = new BankThread[numThreads];
        // int depThreads = 0; // REMOVED: Not needed. Only used to name threads.
        // int withThreads = 0; // REMOVED: Not needed. Only used to name threads.

        // ADDED: Set to "constants" as opposed to randomly generated inside run()
        int depositAmount = 100;
        int withdrawalAmount = 20;

        int numberOfTransactions = 100; // ADDED: Provided to the thread, as opposed to set inside run(). This is so that testing can be automated further such that the expected final balance can be calculated
        
        System.out.println ("Initial balance: $" + balance);
        
        for (int i = 0; i < numThreads; i ++) {
            
            // CHANGED: Naming threads using the index number "i"
            if (i % 2 == 0) { // COMMENT: All even-numbered threads will perform deposits
            
                // depThreads ++; // REMOVED: Not needed. Only used to name threads.
                // threads [i] = new Thread (new BankThread ("Deposit Thread " + (i+1), account, true)); // CHANGED: Create BankThread objects, not Thread objects directly
                // threads [i] = new BankThread ("Deposit Thread " + (i+1), account, true); // ADDED: Amount to be deposited is passed to the thread
                threads [i] = new BankThread ("Deposit Thread " + (i+1), account, true, depositAmount, numberOfTransactions);
  
            } else { // COMMENT: All odd-numbered threads will perform widthrawals
  
                // withThreads ++; // REMOVED: Not needed. Only used to name threads.
                // threads [i] = new Thread(new BankThread ("Withdraw Thread " + (i+1), account, false)); // CHANGED: Create BankThread objects, not Thread objects directly
                // threads [i] = new BankThread ("Withdraw Thread " + (i+1), account, false); // ADDED: Amount to be deposited is passed to the thread
                threads [i] = new BankThread ("Withdraw Thread " + (i+1), account, false, withdrawalAmount, numberOfTransactions);
  
            }
  
            threads [i].start ();
        }
        
        // COMMENT: Wait for all threads to finish before fetching the final account balance
        for (int i = 0; i < numThreads; i ++) {
            
            try {
                threads [i].join ();
            } catch (Exception e) {
                System.out.println (e.getMessage ());
            }

        }

        String message = "Final balance: $" + account.getBalance ();
        System.out.println(message);
        
        // return message; // REMOVED: The main method must return void
        
    }
}