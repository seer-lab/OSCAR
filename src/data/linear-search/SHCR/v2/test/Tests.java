import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import java.util.Collections;

public class Tests {

	private int stackSize;
	private int numThreads;
	private int numNeedles;
	private String needleValue;
	private List<CustomObject> stack;

	@Before
	public void dataSetUp() {

		this.numThreads = Runtime.getRuntime().availableProcessors() + 1;
		this.stackSize = 10000;
        this.numNeedles = 100;
        this.needleValue = "TARGET";

        stack = generateStack(stackSize, needleValue, numNeedles);
	}

    @Test
    public void testTargets() {

		// Create and start threads
        List<SearchThread> runnables = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            SearchThread newRunnable = new SearchThread(i+1, stack, needleValue);
            runnables.add(newRunnable);
            Thread newThread = new Thread(newRunnable);
            threads.add(newThread);
            newThread.start();
        }

        // Wait for all threads to terminate
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {}
        }

        int totalChecked = 0;
        int totalNeedlesFound = 0;
        for (SearchThread runnable : runnables) {
            totalChecked += runnable.numObjectsChecked;
            totalNeedlesFound += runnable.numTargetsFound;
		}
		
		assertEquals(this.numNeedles, totalNeedlesFound);
		assertEquals(this.stackSize, totalChecked);
	}
	
	public static List<CustomObject> generateStack(int size, String targetValue, int numNeedles) {
        
        List<Integer> randomIndexes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            randomIndexes.add(i);
        }
        Collections.shuffle(randomIndexes);
        randomIndexes = randomIndexes.subList(0, numNeedles);

        List<CustomObject> stack = new ArrayList<>();
        for (int i = 0; i < size; i ++) {

            String objectValue;

            if (randomIndexes.contains(i)) {
                
                objectValue = targetValue;
           
            } else {

                // Make sure the random string generator won't accidentally create the target string
                do {
                    objectValue = generateRandomString(targetValue.length());
                } while (objectValue == targetValue);
            }

            stack.add(new CustomObject(objectValue, i+1));
        }

        return stack;
    }

    public static String generateRandomString(int length) {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder string = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int)(alphabet.length() 
            * Math.random());
            string.append(alphabet.charAt(index)); 
        }

        return string.toString();
    }
}