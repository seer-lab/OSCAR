import java.util.LinkedList; 
import java.util.Queue; 

public class Restaurant {

    Queue<PizzaOrder> queue;
    // add()- This method is used to add elements at the tail of queue. More specifically, at the last of linked-list if it is used, or according to the priority in case of priority queue implementation.
    // peek()- This method is used to view the head of queue without removing it. It returns Null if the queue is empty.
    // element()- This method is similar to peek(). It throws NoSuchElementException when the queue is empty.
    // remove()- This method removes and returns the head of the queue. It throws NoSuchElementException when the queue is empty.
    // poll()- This method removes and returns the head of the queue. It returns null if the queue is empty.
    // size()- This method return the no. of elements in the queue.

    int totalPizzasMade;
    int totalPizzasSold;
    int totalOrder;

    Restaurant(int totalPizzasToMake) {
        this.queue = new LinkedList<>();
        this.totalOrder = totalPizzasToMake;
        this.totalPizzasMade = 0;
        this.totalPizzasSold = 0;
    }
}