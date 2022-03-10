public class BankThread extends Thread {

    // private Thread t; // REMOVED: Not used
    private String threadName;
    private boolean task;
    Account account;
    
    int amt; // ADDED: Amount property, so that it can be provided to the thread and not generate it randomly
    int numberOfTransactions; // ADDED: Provided to the thread, as opposed to set inside run(). This is so that testing can be automated further such that the expected final balance can be calculated

    // BankThread (String name, Account a, boolean tk) { // ADDED: Amount property, so that it can be provided to the thread and not generate it randomly
    BankThread (String name, Account a, boolean tk, int amount, int nTransactions) {
        
        // account = a; // CHANGED: Referring to all member variables using "this."
        this.account = a;
        // threadName = name; // CHANGED: Referring to all member variables using "this."
        this.threadName = name;
        // task = tk; // CHANGED: Referring to all member variables using "this."
        this.task = tk;

        this.amt = amount; // ADDED: Amount property, so that it can be provided to the thread and not generate it randomly
        this.numberOfTransactions = nTransactions; // ADDED: Provided to the thread, as opposed to set inside run(). This is so that testing can be automated further such that the expected final balance can be calculated
    }

    public void run () {
        
        // for (int i = 0; i < 100; i ++) { // CHANGED: The number of times to perform a transaction is now provided to the thread, as opposed to set inside run(). This is so that testing can be automated further such that the expected final balance can be calculated
        for (int i = 0; i < this.numberOfTransactions; i ++) {

            // int amt; // REMOVED: Added as a class member instead
            // int balance; // REMOVED: Not sure why this is stored here, and not just read-in inside the critical section...

            // synchronized (account) { // CHANGED: Referring to all member variables using "this."
        /* MUTANT : "RSB (Removed Synchronized Block)" */

            // amt = ThreadLocalRandom.current ().nextInt (1, 1000 + 1); // CHANGED: Switching randomly-generated values out for constant variables so they can be properly tested. See BankThread constructor.
            // balance = account.getBalance (); REMOVED: Not needed - just access it through the account object
            // account.applyTransaction (threadName, amt, task); // CHANGED: Referring to all member variables using "this."
            this.account.applyTransaction (threadName, amt, task);

            if (task) {
                // System.out.println (threadName + " deposited $" + amt + "\nNew balance is $" + (balance + amt) + "\n"); // CHANGED: Access the account balance through the object member or getter, as opposed to first storing it in an intermediate variable outside of the critical region
                System.out.println (threadName + " deposited $" + amt + "\nNew balance is $" + (account.getBalance() + amt) + "\n");
            } else {
            
                // if (amt > balance) System.out.println ("Error: " + threadName + " trying to withdraw $" + amt + " when balance is only $" + balance + "\n"); // CHANGED: Access the account balance through the object member or getter, as opposed to first storing it in an intermediate variable outside of the critical region
                if (amt > account.getBalance()) System.out.println ("Error: " + threadName + " trying to withdraw $" + amt + " when balance is only $" + account.getBalance() + "\n");
                else {
                    // System.out.println (threadName + " withdrew $" + amt + "\nNew balance is $" + (balance - amt) + "\n"); // CHANGED: Access the account balance through the object member or getter, as opposed to first storing it in an intermediate variable outside of the critical region
                    System.out.println (threadName + " withdrew $" + amt + "\nNew balance is $" + (account.getBalance() - amt) + "\n");
                }

            }
            
            // REMOVED: This is not necessary - just more noise
            // try {
            //     Thread.sleep (10);
            // } catch (Exception e) {
            //     System.out.println (e.getMessage ());
            // }
        }
    }

    // REMOVED: This is not necessary - only used for printing
    // public void start () {
    //     System.out.println ("Initializing thread " + threadName);
    //     if (t == null) {
    //         t = new Thread (this, threadName);
    //         t.start ();
    //     }
    // }
}