import java.util.List;

public class SearchThread implements Runnable {

    public int id;
    private String needleValue;
    private List<CustomObject> stack;
    public int numObjectsChecked;
    public int numTargetsFound;

    SearchThread(int id, List<CustomObject> sharedList, String targetValue) {

        this.id = id;
        this.stack = sharedList;
        this.needleValue = targetValue;
        this.numObjectsChecked = 0;
        this.numTargetsFound = 0;
    }

    @Override
    public void run() {

        System.out.printf("Thread %d is searching...\n", this.id);

        for (CustomObject object : this.stack) {

            /* MUTANT : "SKCR (Shrink Critical Region)" */
            if (object.isChecked())
                continue;

            synchronized (object) {
                
                if (object.getValue() == this.needleValue)
                    this.numTargetsFound++;

                object.toggleChecked();
            }

            /* MUTANT : "SKCR (Shrink Critical Region)" */
            this.numObjectsChecked++;
        }
    }
}