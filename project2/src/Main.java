
import java.util.ArrayList;
import java.util.List;

/**
 * Steven Paustian
 * CS353
 * Project 2
 * 11/04/2014
 */


/*
  Main initiailizes the number of nodes based on the
  first argument passed to it.  Nodes are created in
  a Thread List, and started.  Everything else is handled
  by the Nodes and Monitor.
  */

public class Main {

    // Port numbers for node's server socket will be
    // port_number_base + Node.id
    public static int port_number_base = 9100;
    public static final String HOSTNAME = "127.0.0.1";
    public static int num_nodes;

    //Token Holding Time = Max Frame Size * 5
    public static final int THT = 5044;

    public static void main(String[] args) {
      //Ensure correct usage
      String ant_args = System.getProperty("prop1");
      check_args(ant_args);
      
      num_nodes = Integer.parseInt(ant_args);

      List<Thread> token_ring = new ArrayList();

      // Initialize threads
      for(int i = 0; i <= num_nodes; i++){
        final int j = i;

        if(i == 0) {
          token_ring.add(i, new Thread() {
            public void run() {
              new Monitor(j);
              return;
            }
          });
        }
        else {
          token_ring.add(i, new Thread() {
            public void run() {
              new Node(j);
              return;
            }
          });
        }
      }

      // Run Threads
      for(int i = 0; i < token_ring.size(); i++)
        token_ring.get(i).start();
    }

    // Ensure args are correct
    public static void check_args(String args){
      if(args == null) usage();

      //Ensure second arg is an integer
      try{
        Integer.parseInt(args);
      }catch(NumberFormatException e){
        usage();
      }
    }

    // Show correct usage when bad usage detected
    public static void usage(){
      System.out.println("Usage: ");
      System.out.println("java TokenRing some_integer");
      System.out.println("Where: 1 < some_integer < 255");
      System.out.println("Exiting...");

      System.exit(1);
    }
}
