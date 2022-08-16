/**
 * @author Golan
 * 17/10/2003
 * 11:59:43
 * @version 1.0
 */


import java.io.*;


/**
 *
 */
public class ProgramRunner {


  private BuggedProgram bug;


  private int threadsNumber;


  /**
   * @param threadsNumber
   */
  public ProgramRunner(int threadsNumber) {
    this.threadsNumber = threadsNumber;
    this.bug = new BuggedProgram(threadsNumber);
  }


  public void doWork() {
    String newLine = System.getProperty("line.separator");
    System.out.println("Number Of Threads: " + this.threadsNumber + " Number Of Bugs: ");
    this.bug.doWork();
  }


  public static void main(String[] args) {
    //File output = new File("output.txt");

    //DataOutputStream out = null;
    //FileOutputStream os = new FileOutputStream(output);
    //out = new DataOutputStream(os);

    String newLine = System.getProperty("line.separator");
    System.out.println("In this file you will find the number of the bug appearances " + "accordingly to the number of threads that the " + "bugged program utilized with:" + newLine + newLine);

    System.out.println("Few Threads: " + newLine + newLine);
    ProgramRunner fewThreads = new ProgramRunner(17);
    fewThreads.doWork();
    System.out.println(newLine + "************************************" + newLine + newLine);
    System.out.println("Average Threads: " + newLine + newLine);
    ProgramRunner averageThreads = new ProgramRunner(40);
    averageThreads.doWork();
    System.out.println(newLine + "************************************" + newLine + newLine);
    System.out.println("A Lot Of Threads: " + newLine + newLine);
    ProgramRunner aLotOfThreads = new ProgramRunner(120);
    aLotOfThreads.doWork();
    System.out.println(newLine + "************************************" + newLine + newLine);


  }


}
