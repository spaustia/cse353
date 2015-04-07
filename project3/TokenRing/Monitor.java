import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

/**
 * Steven Paustian
 * CS353
 * Project 2
 * 11/04/2014
 */

/*
  The monitor is a special case of a Node.  It handles garbled
  and orphaned frames.  It also detects when the token is lost an 
  re-issues a token if necessary.
*/
  
public class Monitor extends Node{

  // Holds the finished nodes
  private ArrayList nodes_that_are_finished;

  // Detecting lost token
  private int new_token_time;
  private Timer token_timer;

  public Monitor(int id, int right_neighbor_port_num, int ring_num){
    super(id, right_neighbor_port_num, false, ring_num);

    // No nodes are finished
    nodes_that_are_finished = new ArrayList();

    new_token_time = (Main.num_nodes * 25);

    listen_for_bridge();

    try{
      Thread.sleep(2000);
    }catch(InterruptedException e){}

    create_token();
    listen();
  }

  // Create a token when ring is started, or token lost
  public void create_token(){
    Frame token = new Frame(true);
    transmit_frame(token.to_binary());

    // Restart timer
//    restart_token_timer();
  }

  private void restart_token_timer(){
    if(token_timer != null)
      token_timer.cancel();
    
    token_timer = new Timer();
    token_timer.schedule(new IssueNewToken(), new_token_time);
  }

  // Overwrite Node listen method as Monitor has different responsibilities
  protected void listen(){
    open_input_stream();
    String input;

    while(true) {
      try {
        while ((input = socket_reader.readLine()) != null) {
          if(input.charAt(0) != '0' || input.charAt(0) != '1') continue;
          if (is_token(input)) {
            System.out.println("Monitor has received token!");
            if(input.charAt(0) == '0' && input.charAt(1) == '1') System.exit(0);
//            restart_token_timer();
//            Frame token = new Frame(true);

            // Check if nodes are finished
            input = is_node_finished(input);
            transmit_frame(input);
          }
          else if (frame_M_bit(input) || is_garbled(input)) {
            //Do Nothing i.e. drain the frame
          }
          else { // If not a token, set M bit
            print("Received frame from bridge: " + input);
            input = set_M_bit(input);

            transmit_frame(input);
          }
        }
      } catch (IOException e) {
        e.printStackTrace(System.out);
        System.exit(3);
      }
    }
  }

  protected void listen_for_bridge(){
    open_input_stream();
    String input;

    while(true){
      try{
        while((input = socket_reader.readLine()) != null){

          // Make bridge port num look look like token
          // so nodes pass it on without entering transmit state
          Frame temp_frame = new Frame("0", String.valueOf(input.length()), input, 0);
          String frame = "0001" + temp_frame.to_binary().substring(4);

//          print("data size substring: " + frame.substring(32, 40));
//          print("string value of: " + String.valueOf(input.length()));
//          print("binary frame: " + frame);
//          print("is token: " + is_token(frame));
//          print("binary frame length " + frame.length());
//          print("data size: " +frame_data_size(frame));
//          print("data : " +frame_data(frame));
//          System.exit(1);
          transmit_frame(frame);
          break;
        }
        break;
      }catch(IOException e){
        print("Error listening for bridge frame setup.");
        e.printStackTrace();
        System.exit(id);
      }
    }
  }

  // Save which nodes are finished
  private String is_node_finished(String token){
    String node_id_s = "";
    try{
      node_id_s = token.substring(token.length() - 8);
    }catch(StringIndexOutOfBoundsException e){
      print("TOKEN: " + token);
      print("token length: " + token.length());
      System.exit(id);
    }
    int node_id = Integer.parseInt(node_id_s, 2);

    if(node_id == 0) return token;
    else print("Node: " + node_id + " has alerted finished");

    if(!nodes_that_are_finished.contains(node_id)) nodes_that_are_finished.add(node_id);

    // Exit when finished
    if (is_all_finished()){
      System.out.println("Monitor has detected that all nodes are finished transmitting, exiting...");
      return("111" + token.substring(4));
//      // Exit if
//      System.exit(1);
    }
    return token;
  }

  // Check to see if any more nodes have frames to send
 private boolean is_all_finished(){
   for(int i = 0;i < nodes_that_are_finished.size(); i++){
     System.out.println("All finished, i: " + i +
             " nodes that are finished: " + nodes_that_are_finished.get(i));
     System.out.println("Main.num node: " + Main.num_nodes);
   }
  return(nodes_that_are_finished.size() == Main.num_nodes);
 }

 //Set the M bit
 private String set_M_bit(String frame){
   frame = frame.substring(0,4) + "1" + frame.substring(5, frame.length());
   return frame;
 }

  // Check if the M bit is set
  private boolean frame_M_bit(String frame){
    try {
      return (1 == Integer.parseInt(frame.substring(4, 5), 2));
    }catch(NumberFormatException e){
      print(frame);
      print(frame.substring(4,5));
      System.exit(id);
    }
    return true;
  }

  // The class that gets created when the token timer expires
  class IssueNewToken extends TimerTask{
    public void run(){
      System.out.println("Monitor has sensed a lost token, re-issuing...");
      create_token();
    }
  }
}
