import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;

// Writes to nowhere
class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {}
}

public class RunTests {

    public static void main(String[] args) {

        System.out.println();

        // Avoid printing System.out or System.err streams to the terminal during testing
        PrintStream nullStream = new PrintStream(new NullOutputStream());

        PrintStream originalOut = System.out;        
        System.setOut(nullStream);

        PrintStream originalErr = System.err;
        System.setErr(nullStream);

        Result result = JUnitCore.runClasses(Tests.class);
        System.setOut(originalOut);
        System.setErr(originalErr);

        // Print final test results (e.g. pass/fail)
        System.out.print("Test run status: ");
        if (result.wasSuccessful()) System.out.println("\u001B[1;32mPASSED\u001B[0m");
        else System.out.println("\u001B[1;31mFAILED\u001B[0m");

        // Print failures, if any
        for (Failure failure : result.getFailures()) {
            System.out.println("-> " + failure.toString());
        }

        try {
            // Output results to file
            String outputFilename = args[0];

            FileWriter fileWriter = null;

            try {

                fileWriter = new FileWriter(outputFilename, true);
                
                // Append new test outcome to the file
                if (result.wasSuccessful()) fileWriter.write("P");
                else fileWriter.write("F");

            } catch(IOException e) {

                System.err.println(e.getMessage());

            } finally {
                
                try {
                    if (fileWriter != null) fileWriter.close();

                } catch (IOException e) {
                    // System.err.println(e.getMessage());
                }

            }
        } catch (Exception e) { }

        System.out.println();
    }
}
