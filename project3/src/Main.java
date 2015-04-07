
import java.io.*;
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
    public static int port_number_base = 8200;
    public static final String HOSTNAME = "127.0.0.1";
    public static int num_nodes;

    //Token Holding Time = Max Frame Size * 5
    public static final int THT = 5044;

    public static void main(String[] args) {
      //Ensure correct usage
//      check_args();
//      final String conf_file = System.getProperty("prop1");

      final String conf_file = args[0];
      System.out.println("args length: " + args.length);
      BufferedReader conf_file_reader = open_conf_file(conf_file);
      ArrayList id_array = new ArrayList();

      String input;
      try {
        while ((input = conf_file_reader.readLine()) != null) {
          id_array.add(input);
        }
      }catch(IOException e){
        System.out.println("Error in main reading from conf_file: " + conf_file);
        e.printStackTrace(System.out);
        System.exit(-1);
      }

      num_nodes = id_array.size();

      final ArrayList final_id_array = new ArrayList(id_array);
      final int ring_num = Integer.parseInt(Character.toString(conf_file.charAt(4)));

      List<Thread> token_ring = new ArrayList();

      // Initialize threads
      for(int i = 0; i < num_nodes; i++){
        final int j = i;

        if(i == 0) { // Monitor
          token_ring.add(i, new Thread() {
            public void run() {
              new Monitor(j-ring_num,
                      Integer.parseInt(final_id_array.get(j).toString()),
                      ring_num
              );
              return;
            }
          });
          token_ring.add(i, new Thread() {
            public void run() {
              new Node(
                      Integer.parseInt(final_id_array.get(j).toString()),
                      Integer.parseInt(final_id_array.get(j+1).toString()),
                      false,
                      ring_num
              );

              return;
            }
          });
        }
        else if(i < (num_nodes - 1)) { // Regular node
          token_ring.add(i, new Thread() {
            public void run() {
              new Node(
                      Integer.parseInt(final_id_array.get(j).toString()),
                      Integer.parseInt(final_id_array.get(j+1).toString()),
                      false,
                      ring_num
              );

              return;
            }
          });
        }
        else{// The last node connects to the bridge, which isn't running yet
          token_ring.add(i, new Thread() {
            public void run() {
              new Node(
                      Integer.parseInt(final_id_array.get(j).toString()),
                      0,
                      true,
                      ring_num
              );

              return;
            }
          });
        }
      }

      // Run Threads
      for(int i = 0; i < token_ring.size(); i++)
        token_ring.get(i).start();
    }

  // Ensure conf file argument exists
  public static void check_args(){
    final String conf_file = System.getProperty("conf");
    if(conf_file == null) usage();

    File file = new File(conf_file);
    if(!file.exists()) usage();
  }

    // Show correct usage when bad usage detected
    public static void usage(){
      System.out.println("Usage: ");
      System.out.println("java TokenRing path_to_conf_file");
      System.out.println("Exiting...");

      System.exit(1);
    }

  // Open ringx.conf for reading
  public static BufferedReader open_conf_file(String conf_file){
    try{
      File input_file;
      input_file = new File("proj_files/" + conf_file);

      BufferedReader input_file_reader;
      input_file_reader = new BufferedReader(new FileReader(input_file));

      return input_file_reader;
    }catch(FileNotFoundException e){
      System.out.println("Error in Main opening conf-file: " + conf_file);
      e.printStackTrace(System.out);
      System.exit(-1);
    }

    return null;
  }
}
