
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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
  private boolean[] nodes_that_are_finished;

  // Detecting lost token
  private int new_token_time;
  private Timer token_timer;

  public Monitor(int id){
    super(id);

    // No nodes are finished
    nodes_that_are_finished = new boolean[Main.num_nodes + 1];
    for(int i = 0; i < nodes_that_are_finished.length; i++)
      nodes_that_are_finished[i] = false;

    nodes_that_are_finished[0] = true;
    new_token_time = (Main.num_nodes * 25);

    create_token();
    listen();
  }

  // Create a token when ring is started, or token lost
  public void create_token(){
    Frame token = new Frame(true);
    transmit_frame(token.to_binary());

    // Restart timer
    restart_token_timer();
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
          if (is_token(input)) {
            System.out.println("Monitor has received token!");
            
            restart_token_timer();

            transmit_frame(input);
            
            // Check if nodes are finished
            is_node_finished(input);
          }
          else if (frame_M_bit(input) || is_garbled(input)) {
            //Do Nothing i.e. drain the frame
          }
          else { // If not a token, set M bit
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

  // Save which nodes are finished
  private void is_node_finished(String token){
    String node_id_s = token.substring(token.length() - 8);
    int node_id = Integer.parseInt(node_id_s, 2);

    if(node_id == 0) return;

    nodes_that_are_finished[node_id] = true;

    // Exit when finished
    if (all_finished()){
      System.out.println("Monitor has detected that all nodes are finished transmitting, exiting...");

      // Exit if 
      System.exit(1);
    }
  }

  // Check to see if any more nodes have frames to send
 private boolean all_finished(){
   for(int i = 0; i < nodes_that_are_finished.length; i++){
     if(nodes_that_are_finished[i] == false) {
       return false;
     }
   }

   return true;
 }
 //Set the M bit
 private String set_M_bit(String frame){
   frame = frame.substring(0,4) + "1" + frame.substring(5, frame.length());
   return frame;
 }

  // Check if the M bit is set
  private boolean frame_M_bit(String frame){
    return (1 == Integer.parseInt(frame.substring(4, 5), 2));
  }

  // The class that gets created when the token timer expires
  class IssueNewToken extends TimerTask{
    public void run(){
      System.out.println("Monitor has sensed a lost token, re-issuing...");
      create_token();
    }
  }
}
