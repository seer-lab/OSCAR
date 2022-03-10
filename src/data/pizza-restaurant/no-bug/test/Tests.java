import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.After;
import static org.junit.Assert.assertEquals;

public class Tests {

	int numTotalThreads;
	int numPizzas;
	int numMakers;
	int numSellers;
    int numPizzasPerThread;
    
    PizzaMaker[] makers;
	PizzaSeller[] sellers;
	Thread[] makerThreads;
	Thread[] sellerThreads;
    Restaurant restaurant;

    int totalPizzasMade;
    int totalPizzasSold;

	@Before
	public void initializeTests() {
		
		this.numTotalThreads = Runtime.getRuntime().availableProcessors() + 1;
        this.numMakers = (int) Math.ceil(this.numTotalThreads*0.7);
        // this.numMakers = 30;
        
        this.numSellers = (int) Math.ceil(this.numMakers/3);
        // this.numSellers = 5;

        this.numPizzas = (this.numMakers * 40);
        // this.numPizzas = 300;

        this.numPizzasPerThread = (int) Math.floor((double) numPizzas/numMakers);

        this.makers = new PizzaMaker[numMakers];
        this.makerThreads = new Thread[numMakers];
        
        this.sellers = new PizzaSeller[numSellers];
        this.sellerThreads = new Thread[numSellers];
        
        this.restaurant = new Restaurant(numPizzas);

        this.totalPizzasMade = 0;
        this.totalPizzasSold = 0;
	}

	// Ensure the pizza-cooking process alone executes the expected number of orders
    @Test
    @Ignore
    public void testMakers() {

		for (int i = 0; i < numMakers; i++) {

            if (i == (numMakers-1)) {
                this.numPizzasPerThread = this.numPizzasPerThread + (this.numPizzas - (this.numPizzasPerThread * this.numMakers));
            }

            this.makers[i] = new PizzaMaker(restaurant, "" + (i + 1), numPizzasPerThread);
            this.makerThreads[i] = new Thread(makers[i]);
            this.makerThreads[i].start();
        }
		
        for (int i = 0; i < this.numMakers; i++) {
            try {
                this.makerThreads[i].join();
                this.totalPizzasMade += this.makers[i].pizzasMade;
            } catch (Exception e) {}
        }
		
		assertEquals(this.numPizzas, totalPizzasMade);
        assertEquals(this.numPizzas, this.restaurant.totalPizzasMade);
        assertEquals(this.numPizzas, this.restaurant.queue.size());
	}
	
	// Ensure the makers and the sellers of the cooked goods are synchronized in their tasks, such that the correct number of pizzas is cooked and sold.
    @Test(timeout=100000)
	public void testMakersSellers() {

		for (int i = 0; i < this.numMakers; i++) {

            if (i == (this.numMakers-1)) {
                this.numPizzasPerThread = this.numPizzasPerThread + (this.numPizzas - (this.numPizzasPerThread * this.numMakers));
            }

            this.makers[i] = new PizzaMaker(this.restaurant, "" + (i + 1), this.numPizzasPerThread);
            this.makerThreads[i] = new Thread(this.makers[i]);
            this.makerThreads[i].start();
        }

        for (int i = 0; i < this.numSellers; i++) {

            this.sellers[i] = new PizzaSeller(this.restaurant, "" + (this.numMakers + i + 1));
            this.sellerThreads[i] = new Thread(this.sellers[i]);
            this.sellerThreads[i].start();
        }
		
        for (int i = 0; i < this.numMakers; i++) {
            try {
                this.makerThreads[i].join();
                this.totalPizzasMade += this.makers[i].pizzasMade;
            } catch (Exception e) {}
        }
        
        for (int i = 0; i < this.numSellers; i++) {
            try {
                this.sellerThreads[i].join();
                this.totalPizzasSold += this.sellers[i].pizzasSold;
            } catch (Exception e) {}
        }

        assertEquals(this.numPizzas, totalPizzasSold);
        assertEquals(totalPizzasMade, totalPizzasSold);
        assertEquals(0, this.restaurant.queue.size());
    }

    @After
	public void printStats() {
        System.out.println();
        System.out.println("+-------------------------------------------");
        System.out.println("| FINAL STATS");
        System.out.println("+-------------------------------------------");
        System.out.println("| Pizzas cooked (from workers): " + totalPizzasMade);
        System.out.println("| Pizzas cooked (from restaurant): " + restaurant.totalPizzasMade);
        System.out.println("| Pizzas sold (from workers): " + totalPizzasSold);
        System.out.println("| Pizzas sold (from restaurant): " + restaurant.totalPizzasSold);
        System.out.println("| Orders in queue: " + restaurant.queue.size());
        System.out.println("+-------------------------------------------\n");
    }
}