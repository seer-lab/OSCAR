

class TmemoryHandler extends Thread {

  public void run() {
    while (Manager.request_counter > 0) {
      System.out.print("\rMemory Blocks to be realesed yet: " + Manager.request_counter + "     ");
      while (!Manager.flag) {
        Thread.yield();
        //try {sleep (10);} catch (Exception e) {}

      }
      Manager.request_counter--;
      Manager.flag = false;
    }
    System.out.println("\nTmemoryHandler thread finish");
    new Thread(new Monitor()).start();
  }

  public static class Monitor implements Runnable {
    @Override
    public void run() {
      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      System.out.println("deadlock");
      System.out.flush();
      System.exit(0);
    }
  }
}


