import java.util.NoSuchElementException;
import java.util.Random;

public class PizzaSeller implements Runnable {

    Restaurant restaurant; // Shared variable
    String staffID;
    int pizzasSold;

    PizzaSeller(Restaurant restaurant, String staffID) {
        this.restaurant = restaurant;
        this.staffID = staffID;
        this.pizzasSold = 0;
    }

    @Override
    public void run() {

        System.out.println ("Seller " + this.staffID + " is trying to sell some pizza");
        sellPizza();
        System.out.println ("Seller " + this.staffID + " is done");
    }

    public void sellPizza() {
        
        Random randomObj = new Random();

        /* MUTANT : "MSP (Modify Synchronized Block Parameter)" */
        synchronized (this) {

            while (restaurant.totalPizzasSold < restaurant.totalOrder) {

                boolean isSellSuccessful = randomObj.nextInt(2) == 1;

                if (isSellSuccessful) { // Randomize chances of a successful sell

                    if (restaurant.queue.isEmpty()) {
                        try {
                            restaurant.wait();
                            System.out.println("\nSeller " + this.staffID + " is waiting for more pizzas to be made ...");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        PizzaOrder pizzaSold = restaurant.queue.remove(); // Remove head of the queue
                        this.pizzasSold++;
                        restaurant.totalPizzasSold++;
                        restaurant.notifyAll();

                        System.out.println ("\nSeller " + this.staffID + " sold a " + pizzaSold.type + " pizza");
                        System.out.println("Total pizza order: " + restaurant.totalOrder + ", pizzas sold: " + restaurant.totalPizzasSold);
                        System.out.println("There are now " + restaurant.queue.size() + " pizzas in the order queue.");
                    } catch (NoSuchElementException e) {}
                }
            }
        }

        System.out.println("test");
    }
}