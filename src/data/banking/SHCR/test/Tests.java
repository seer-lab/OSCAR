import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Tests {

    // REMOVED: Removed all original tests, as they don't pertain at all to the account balance values expected
    // Bank b = new Bank ();

    // @Test
    // public void testIncorrectBalance () {
    //     String [] args;
    //     args = new String [] {"-1", "2"};
    //     assertEquals ("Error: Initial balance should be positive.", b.main (args));
    // }

    // @Test
    // public void testIncorrectThreadCount () {
    //     String [] args;
    //     args = new String [] {"1", "-1"};
    //     assertEquals ("Error: Should have at least 2 threads.", b.main (args));
    // }

    // @Test
    // public void testCorrectOutput () {
    //     String [] args;
    //     args = new String [] {"1000", "2"};
    //     assertEquals (true, b.main (args).matches ("Final balance: $[0-9]+"));
    // }

    // ADDED: Test final balance after all transactions are performed
    @Test
    public void testFinalBalance() {

        // Usually command-line arguments
        int numberOfThreads = 5;
        int initialBalance = 1000;

        // Shared object
        Account account = new Account(initialBalance);
        
        // ADDED: Set to "constants" as opposed to randomly generated inside run()
        int depositAmount = 100;
        int withdrawalAmount = 20;

        int numberOfTransactions = 100; // Provided to the thread, as opposed to set inside run(). This is so that testing can be automated further such that the expected final balance can be calculated

        int numberOfDepositThreads = 0;
        int numberOfWithdrawalThreads = 0;

        // Generate threads that are to perform deposit and withdrawal transactions
        // Taken from the original main function
        BankThread allThreads[] = new BankThread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i ++) {
            if (i % 2 == 0) {
                numberOfDepositThreads++;
                allThreads[i] = new BankThread ("Deposit Thread " + (i+1), account, true, depositAmount, numberOfTransactions);
            } else {
                numberOfWithdrawalThreads++;
                allThreads[i] = new BankThread ("Withdraw Thread " + (i+1), account, false, withdrawalAmount, numberOfTransactions);
            }
            allThreads[i].start();
        }

        // Wait for all threads to finish before fetching the final account balance
        for (int i = 0; i < numberOfThreads; i ++) {
            
            try {
                allThreads[i].join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Dynamically calculate what the final balance SHOULD be
        int expectedFinalBalance = initialBalance + (numberOfDepositThreads * depositAmount * numberOfTransactions) - (numberOfWithdrawalThreads * withdrawalAmount * numberOfTransactions);
        assertEquals(expectedFinalBalance, account.getBalance());
    }
}