import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) {

		// CHANGED: Variables should be "constant", instead of entered by the user
		double oversellPercent = 0.05; // 5%
		int baseticketsAvailable = 1000;
		int numberOfThreads = 10;

		int ticketsAvailable = (int) (baseticketsAvailable * (1 + oversellPercent));

		TicketNumber tickets = new TicketNumber(ticketsAvailable);
		List<TicketSeller> sellers = new ArrayList<TicketSeller>();
		List<Thread> threads = new ArrayList<Thread>();

		for(int i = 0; i < numberOfThreads; i++) {
			sellers.add(new TicketSeller(tickets, i+1));
			threads.add(new Thread(sellers.get(i), "thread " + (i + 1)));
			threads.get(i).start();
		}

		try {
			for(Thread thread : threads){
				thread.join();
			}
		} catch (Exception e){
			System.out.println(e);
		}

		int actualSoldTotal = 0;
        for (int i = 0; i < numberOfThreads; i++) {
            actualSoldTotal += sellers.get(i).agentSold;
        }

		System.out.println("Ticket Sales Complete - " + tickets.getTicketsSold() + " tickets sold");
		System.out.println("Real sale: " + actualSoldTotal);
	}
}
