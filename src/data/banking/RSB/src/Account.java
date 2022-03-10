public class Account {

    int balance;

    Account (int b) {
        // balance = b; // CHANGED: Referring to all member variables using "this."
        this.balance = b;
    }

    public int getBalance () {
        // return balance; // CHANGED: Referring to all member variables using "this."
        return this.balance;
    }

    public void applyTransaction (String threadName, int amount, boolean task) {
    
        try {
    
            // if (task) balance += amount; // CHANGED: Referring to all member variables using "this."
            if (task) this.balance += amount;
            // else if (amount < balance) balance -= amount; // CHANGED: Referring to all member variables using "this."
            else if (amount < this.balance) this.balance -= amount;

        } catch (Exception e) {
            System.out.println (e.getMessage ());
        }
    }
}