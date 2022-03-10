import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class Search {

    public static void main(String [] args) {

        // ADDED: Extracted magic numbers into variables
        String searchPath;
        String fileExtension;
        int numThreads;

        WorkerController workerController;

        if (args.length == 3) {

            // ADDED: Extracted magic numbers into variables
            searchPath = args[0];
            fileExtension = args[1];
            numThreads = Integer.parseInt(args[2]);

            // workerController = new WorkerController(searchPath, args [1], Integer.parseInt(args [2])); // CHANGED: Extracted arguments into variables for better readibility - see below

        } else {
            // ADDED: Extracted arguments into variables for better readibility
            // String searchPath = "./";
            searchPath = "./test-files/";  // CHANGED: Using test files
            fileExtension = ".java";
            numThreads = 5;
            // numThreads = Runtime.getRuntime().availableProcessors() + 1; // CHANGED: This is often the most optimal number of threads

            // workerController = new WorkerController("./", ".java", 1); // CHANGED: Extracted arguments into variables for better readibility - see below
        }

        workerController = new WorkerController(searchPath, fileExtension, numThreads); // ADDED: Extracted arguments into variables for better readibility - see above

        workerController.startWorker(); // ADDED COMMENT: Start the specified number of threads above
        workerController.joinThread(); // ADDED COMMENT: Wait for all threads to finish

        // ADDED COMMENT: Get the "results" queue, where all workers should have stored their findings
        // ConcurrentLinkedQueue <String> results = workerController.getResults(); // CHANGED: Using a regular queue instead
        Queue <String> results = workerController.getResults();

        System.out.println("Found files containing the word: " + workerController.getPattern());

        // ADDED COMMENT: Print all the files that match the pattern/extension specified above
        for (String fileDirectory : results) {
            System.out.println(fileDirectory);
        }

        // ADDED: Printing
        System.out.printf("\nFound %d files that matched the pattern \"%s\"\n", results.size(), fileExtension);
    }
}
