

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Golan
 * 17/10/2003
 * 12:01:05
 * @version 1.0
 */


public class BuggedProgram {


  private int threadNumber;


  private PingPong pingPongPlayer;


  private int bugAppearanceNumber = 0;


  /**
   *
   */
  public BuggedProgram(int threadNumber) {
    this.threadNumber = threadNumber;
    this.pingPongPlayer = new PingPong();
  }


  /**
   * create some thread from type <code>BugThread</code> and
   * when the last thread is finished - write the value of <code>BugThread-->variable</code>
   * to the output file
   */
  public void doWork(int threadsNumber) {

    ArrayList threads = new ArrayList();
    for (int i = 0; i < threadNumber; i++) {
      BugThread t = new BugThread(this);
      t.start();
      threads.add(t);
    }
    //threads are still running!

    Iterator iterator = threads.iterator();
    while (iterator.hasNext()) {
      Thread t = (Thread) iterator.next();
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
    }
    //threads completed their running!!!

    String newLine = System.getProperty("line.separator");
    System.out.println(String.valueOf(this.bugAppearanceNumber + " bugs. " + newLine));
    System.out.println("Bug appearance Number" + this.bugAppearanceNumber);

    if (bugAppearanceNumber > 0)
      System.out.println("BUG for " + threadsNumber + " threads");
  }


  /**
   *
   */
  public void pingPong() {
    try {
      this.pingPongPlayer.getI();
      PingPong newPlayer;
      newPlayer = this.pingPongPlayer;
      this.pingPongPlayer = null;
      long time = System.currentTimeMillis();
      while ((System.currentTimeMillis() - time) < 50)
        ;
      this.pingPongPlayer = newPlayer;
    } catch (NullPointerException e) {
      this.bugAppearanceNumber++;
    }

  }


}



