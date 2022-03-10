import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import java.util.Queue;
import java.io.File;

public class Tests {

    int numDirs;
    int numFilesPerDir;

	@Before
	public void initializeTests() {

        numDirs = 10;
        numFilesPerDir = 100;

        File testFileDir = new File("./test-files");

        // If the test data is already present, then just skip this set-up and continue with the test
        if (testFileDir.exists()) return;

        String pythonScript = "generate-test-files.py";
        
        try {

            // Run Python script
            Process pythonProcess = Runtime.getRuntime().exec("python3 " + pythonScript + " " + testFileDir + " " + numDirs + " " + numFilesPerDir);
            int exitCode = pythonProcess.waitFor();

            // In the case there was an issue with the python script
            if (exitCode != 0)
                System.exit(1);

        } catch (Exception e) {
            System.err.printf("Unable generate test data via Python script %s\n", pythonScript);
            e.printStackTrace();
            System.exit(1);
        }
	}

    @Test
	public void testSearchFileCount() {

        String searchPath = "./test-files/";
        String fileExtension = ".java";
        int numThreads = Runtime.getRuntime().availableProcessors() + 1;

        WorkerController workerController;
        
        workerController = new WorkerController(searchPath, fileExtension, numThreads);
        workerController.startWorker();
        workerController.joinThread();

        // Get the "results" queue, where all workers should have stored their findings
        Queue <String> results = workerController.getResults();

        // ADDED: Printing
        System.out.printf("\nFound %d files that matched the pattern \"%s\"\n", results.size(), fileExtension);

        int expectedNumFilesFound = numDirs * numFilesPerDir;
        assertEquals(expectedNumFilesFound, results.size());
    }
}