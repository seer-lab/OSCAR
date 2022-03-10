public class Main {
    
    public static void main(String[] args) {

        int numTotalThreads = Runtime.getRuntime().availableProcessors() + 1;

        int numPizzas = 300;

        int numMakers = 50;
        // int numMakers = (int) Math.ceil(numTotalThreads*0.7);
        int numSellers = 5;
        // int numSellers = (int) Math.floor(numTotalThreads*0.3);

        int numPizzasPerThread = (int) Math.floor((double) numPizzas/numMakers);

        PizzaMaker[] makers = new PizzaMaker[numMakers];
        Thread[] makerThreads = new Thread[numMakers];
        
        PizzaSeller[] sellers = new PizzaSeller[numSellers];
        Thread[] sellerThreads = new Thread[numSellers];
        
        Restaurant restaurant = new Restaurant(numPizzas);

        for (int i = 0; i < numMakers; i++) {

            int numPizzasToMake = numPizzasPerThread;

            if (i == (numMakers-1)) {
                numPizzasToMake = numPizzasPerThread + (numPizzas - (numPizzasPerThread * numMakers));
            }

            makers[i] = new PizzaMaker(restaurant, "" + (i + 1), numPizzasToMake);
            makerThreads[i] = new Thread(makers[i]);
            makerThreads[i].start();
        }

        for (int i = 0; i < numSellers; i++) {

            sellers[i] = new PizzaSeller(restaurant, "" + (numMakers + i + 1));
            sellerThreads[i] = new Thread(sellers[i]);
            sellerThreads[i].start();
        }

        int totalPizzasMade = 0;
        for (int i = 0; i < numMakers; i++) {
            try {
                makerThreads[i].join();
                totalPizzasMade += makers[i].pizzasMade;
            } catch (Exception e) {}
        }

        int totalPizzasSold = 0;
        for (int i = 0; i < numSellers; i++) {
            try {
                sellerThreads[i].join();
                totalPizzasSold += sellers[i].pizzasSold;
            } catch (Exception e) {}
        }
        
        System.out.println();
        System.out.println("+---------------------------------");
        System.out.println("| FINAL STATS");
        System.out.println("+---------------------------------");
        System.out.println("| Pizzas cooked (from workers): " + totalPizzasMade);
        System.out.println("| Pizzas cooked (from restaurant): " + restaurant.totalPizzasMade);
        System.out.println("| Pizzas sold (from workers): " + totalPizzasSold);
        System.out.println("| Pizzas sold (from restaurant): " + restaurant.totalPizzasSold);
        System.out.println("| Orders in queue: " + restaurant.queue.size());
        System.out.println("+---------------------------------");
    }
}