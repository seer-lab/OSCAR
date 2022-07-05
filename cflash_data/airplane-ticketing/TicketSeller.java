import java.util.Random;

class TicketSeller implements Runnable {
	protected TicketNumber tickets;
	protected int agentNumber;
	int agentSold; // ADDED: To calculate the real number of tickets sold per agent and overall
	
	public TicketSeller(TicketNumber tickets, int agentNumber) {
		this.tickets = tickets;
		this.agentNumber = agentNumber;
		this.agentSold = 0; // ADDED: To calculate the real number of tickets sold per agent and overall
	}
	
	public void run() {
		boolean loop = true;
		Random rand = new Random();
		int transactionPercent;
		if((agentNumber % 2) == 0){
			transactionPercent = 45;
		} else {
			transactionPercent = 30;
		}
		int ticketMax = 4;
		while(loop){
			int randomNum = rand.nextInt(100);
			if(randomNum < transactionPercent){
				int ticketNum = rand.nextInt(ticketMax) + 1;
				if(tickets.updateTickets(ticketNum)){
					System.out.println("Ticket Agent " + agentNumber + ": Successful Transaction - " 
									   + ticketNum + " tickets sold");	
					this.agentSold += ticketNum; // ADDED: To calculate the real number of tickets sold per agent and overall
				} else {
					System.out.println("Ticket Agent " + agentNumber + ": Unsuccessful Transaction");
				}
				if(tickets.soldAllTickets()){
					loop = false;
				}
			} else {
				System.out.println("Ticket Agent " + agentNumber + ": Unsuccessful Transaction");
			}
		}
	}
}
