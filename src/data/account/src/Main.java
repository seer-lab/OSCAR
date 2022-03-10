import java.util.ArrayList;

public class Main {

    private static Account[] bank;
    private static AccountThread[] threads;

    public static void main(String[] args) {

        int numAccounts = 4;
        int userInput = -1;
        if (args.length > 0) {
            try {
                userInput = Integer.parseInt(args[0]);
            } catch(Exception e) { }
            if (userInput < 27 && userInput > 0) numAccounts = userInput;
        }

        bank = new Account[numAccounts];
        threads = new AccountThread[numAccounts];
        char letter = 'A'; // name all accounts as A to Z
        for (int i = 0; i < numAccounts; i++) {
            String accName = String.valueOf(letter);
            bank[i] = new Account(accName, i+1, 100);
            threads[i] = new AccountThread(bank[i], bank);
            letter ++;
        }

        // get all threads started
        for (AccountThread thread : threads) thread.start();

        // wait for all threads to finish
        try {
            for (AccountThread thread : threads) thread.join();
        } catch (Exception e) { }

        // print all finalized accounts
        System.out.println("\n" + toStringAll());

    }

    private static String toStringAll() {

        String str = "";
        for (Account account : bank)
            str +=  "Account: " + account.name + " -> balance $" + account.balance + "\n";

        return str;
    }
}