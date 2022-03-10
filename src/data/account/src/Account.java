public class Account {

    public String name;
    public int number;
    public double balance;

    public Account(String name, int number, double balance) {
        this.name = name;
        this.number = number;
        this.balance = balance;
    }

    synchronized void deposit(double amount) {
        this.balance += amount;
        System.out.println("\nDepositing...\n" + "Amount: $" + amount + "\nInto account: " + this.name + " (new balance: $" + this.balance + ")");
    }

    synchronized void withdraw (double amount) {
        this.balance -= amount;
        System.out.println("\nWithdrawing...\n" + "Amount: $" + amount + "\nFrom account: " + this.name + " (new balance: $" + this.balance + ")");
    }

    void transfer(Account toAccount, double amount) {

        Account firstLock;
        Account secondLock;

        if (this.number > toAccount.number) {
            firstLock = this;
            secondLock = toAccount;
        } else {
            firstLock = toAccount;
            secondLock = this;
        }

        synchronized(firstLock) {
            synchronized(secondLock) {
                if (toAccount == this) return;
                this.balance -= amount;
                toAccount.balance += amount;
                System.out.println("\nTransferring...\n" + "Amount: $" + amount + "\nFrom account: " + this.name + " (new balance: $" + this.balance + ")\nTo account "  + toAccount.name + " (new balance: $" + toAccount.balance + ")");
            }
        }
    }
}
