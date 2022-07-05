import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class LinearSearch {

    public static void main(String[] args) {

        int stackSize = 10000;

        int numThreads = 5;
        int numNeedles = 100;

        String needleValue = "TARGET";

        List<CustomObject> stack = generateStack(stackSize, needleValue, numNeedles);

        // Printing general program information
        System.out.printf("---------------------------------\n");
        // System.out.printf("Number of threads: %d\n", numThreads);
        System.out.printf("Stack size: %d\n", stackSize);
        System.out.printf("Number of needles to find: %d\n", numNeedles);
        System.out.printf("Target value: \"%s\"\n", needleValue);
        System.out.printf("---------------------------------\n\n");

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

        System.out.println();
        System.out.printf("All threads terminated\n%d objects were iterated over\n%d needle(s) were found\n", totalChecked, totalNeedlesFound);
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