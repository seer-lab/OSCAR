import java.util.ArrayList;

public class AccountThread extends Thread {

    private Account[] bank;
    private Account account;
    
    public AccountThread(Account account, Account[] bank) {
        super("T" + account.name);
        this.account = account;
        this.bank = bank;
    }

    public void run() {

        System.out.println("\n[" + this.getName() + "] STARTED");

        int currIndex = -1;
        for (int i = 0; i < bank.length; i++) {
            if (bank[i] == this.account) {
                currIndex = i;
                break;
            }
        }

        if (currIndex < 0) System.exit(-1);

            this.account.deposit(220);
            this.account.transfer(bank[(currIndex + 1) % this.bank.length], 20);
            this.account.transfer(bank[(currIndex + 2) % this.bank.length], 30);
            this.account.withdraw(20);

        System.out.println("\n[" + this.getName() + "] FINISHED");
    }
}
