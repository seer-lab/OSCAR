import java.util.Random;

public class PizzaMaker implements Runnable {

    Restaurant restaurant;
    String staffID;
    int numPizzas;
    int pizzasMade;
    
    PizzaMaker(Restaurant restaurant, String staffID, int numPizzasToMake) {
        this.restaurant = restaurant;
        this.staffID = staffID;
        this.numPizzas = numPizzasToMake;
        this.pizzasMade = 0;
    }

    @Override
    public void run() {

        System.out.println ("Chef " +this.staffID + " has started");
        makePizza(this.numPizzas);
        System.out.println ("Chef " +this.staffID + " is done");
    }

    public void makePizza(int numPizzas) {

        Random randomObj = new Random();
        String[] pizzaTypes = new String [] {"Cheese", "Pepperoni", "Veggie", "Canadian", "Hawaiian"};

        while (pizzasMade < numPizzas) {

            String pizzaType = pizzaTypes[randomObj.nextInt(pizzaTypes.length)];
            
            /* MUTANT : "RSB (Removed Synchronized Block)" */
            try {

                restaurant.queue.add(new PizzaOrder(pizzaType));    
                
                this.pizzasMade++;
                restaurant.totalPizzasMade++;

                System.out.println("\nChef " + this.staffID + " made a " + pizzaType + " pizza");
                System.out.println("There are now " + restaurant.queue.size() + " pizzas in the order queue.");

            } catch (Exception e) {
                e.printStackTrace();
            }

            restaurant.notifyAll();
        }

        System.out.println("test maker");
    }
}