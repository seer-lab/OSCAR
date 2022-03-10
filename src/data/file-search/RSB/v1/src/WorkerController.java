import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import java.util.LinkedList;

// public class WorkerController extends Thread { // CHANGED: This class is not really a thread; it has no run() method either. Removed the extension to the Thread class
    public class WorkerController {

    Thread [] t;
    // private ConcurrentLinkedQueue <File> queue; // CHANGED: Using a regular queue instead
    // private ConcurrentLinkedQueue <String> results; // CHANGED: Using a regular queue instead
    private Queue <File> queue;
    private Queue <String> results;
    private String directory;
    private String pattern;
    private int numThread;

    //Constructor
    WorkerController (String directory, String pattern, int numThread) {

        // this.queue = new ConcurrentLinkedQueue <> (); // CHANGED: Using a regular queue instead
        // this.results = new ConcurrentLinkedQueue <> (); // CHANGED: Using a regular queue instead
        this.queue = new LinkedList <> ();
        this.results = new LinkedList <> ();
        this.numThread = numThread;
        this.pattern = pattern;
       
        this.t = new Thread [this.numThread];
        this.directory = directory;
       
        this.queue.add(new File(this.directory)); // ADDED COMMENT: Start off with the "root" directory provided
    
    }

    //This function will start the threads by adding the runnable class
    public void startWorker () {

        for (int i = 0; i < numThread; i ++) {
            
            try {
            
                t [i] = new Thread (new Worker (this.queue, this.results, this.pattern, Integer.toString(i)));
                t [i].start();
                //System.out.println("Thread " + i + " has started!");
                Thread.sleep(10); // REMOVED: Moving this into the run() method of the Worker class
            
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }

    public void joinThread () {
        
        for (int i = 0; i < numThread; i ++) {
        
            try {
                t[i].join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // public String getDirectory () {
    //     return directory;
    // }

    public String getPattern () {
        return pattern;
    }

    public int getNumThread () {
        return numThread;
    }

    public Thread [] getT () {
        return t;
    }

    // public ConcurrentLinkedQueue <String> getResults() { 
    public Queue <String> getResults() {  // CHANGED: Using a regular queue instead
        return results;
    }
}