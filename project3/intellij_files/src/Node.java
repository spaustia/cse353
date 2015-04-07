
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Steven Paustian
 * CS353
 * Project 2
 * 11/04/2014
 */

/*
  Nodes listen to their left neighbor for traffic, and transmit to
  their right neighbors.  
*/

public class Node extends Thread{
  public int id;
  public int priority;
  private String input;

  boolean last_node;
  int ring_num;

  // Socket Variables
  protected ServerSocket server_socket;
  protected Socket client_socket;
  protected Socket neighbor_socket;
  public int port_number;
  public int right_neighbor_port_number;

  //Socket I/O Variables
  protected PrintWriter out_writer;
  protected BufferedReader socket_reader;

  // File I/O variables
  private BufferedReader input_file_reader;
  private File input_file, output_file;
  private FileWriter output_file_writer;
  private BufferedWriter writer;

  // Holds the frames
  List<Frame> frame_list;
  boolean alert_finished; //Has the node alerted the monitor it is done?

  // For random events
  Random random;

  public Node(int id, int right_neighor_port_number, boolean last_node, int ring_num){

    // Init vars
    this.id = id;
    port_number = Main.port_number_base + id;
    this.right_neighbor_port_number = right_neighor_port_number + Main.port_number_base;
    this.last_node = last_node;
    this.ring_num = ring_num;

    alert_finished = false;
    random = new Random();
    // Monitor connects to right neighbor first
    // to initialize the circle
    if(id < 0){
      connect_to_right_neighbor();
      open_server_socket();
    }
    else if(!last_node){
      open_server_socket();
      connect_to_right_neighbor();
    }
    // Last node needs to listen for packet telling it the bridge's port num
    // before it can connect to right neighbor
    else{
      open_server_socket();
      listen_for_bridge();
      connect_to_right_neighbor();
    }


    // Open input and create output file
    if(id  > 0) {
      read_input_file();
      open_output_file();
      listen(); //Start listening
    }
  }

  // Create output-file-id#
  private void open_output_file(){
   output_file = new File("proj_files/output-file-" + id);
      try {
        output_file_writer = new FileWriter(output_file);
        writer = new BufferedWriter(output_file_writer);
      }catch(IOException e){
        e.printStackTrace(System.out);
        System.exit(id);
      }
  }


  protected void listen_for_bridge(){
    open_input_stream();

    while(true){
      try {
        while ((input = socket_reader.readLine()) != null) {
          print("Got bridge port num! " + frame_data(input));
          right_neighbor_port_number = Integer.parseInt(frame_data(input));
          break;
        }
      }catch(IOException e){
        print("Error listening for bridge port num");
        e.printStackTrace();
        System.exit(id);
      }
      break;
    }
  }

  // Listen to left neighbor
  protected void listen(){
    open_input_stream();
    while(true) {
      try {
        while ((input = socket_reader.readLine()) != null) {
          // Handle token
          if(is_token(input)){
            System.out.println("Node " + id + " RECEIVED TOKEN");
            transmit_state();

            transmit_frame(input);
          }
          // Handle frames
          else if(frame_destination_address(input) == id && !is_garbled(input)){
              System.out.println("Received frame for self in Node " + id + " and accepting.");
              input = input.substring(0, input.length() - 2) + "10";
              write_frame(input);

            transmit_frame(input);
          }
          else if(frame_source_address(input) == id){
            if(frame_ACK(input)){
              received_ACK_for_frame(input);
              System.out.println("Received ACK'd frame in node " + id);
            }
          }
          else{
            System.out.println("Received frame in node: " +
                     id + " and passing it on to: " +
                     (right_neighbor_port_number - Main.port_number_base) + " destination_address: "
                     + frame_destination_address(input) +
             ", source_address: " + frame_source_address(input));
            transmit_frame(input);
          }
        }
      } catch (IOException e) {
        e.printStackTrace(System.out);
        System.exit(3);
      }
    }
  }

  // Checks for garbled frame
  protected boolean is_garbled(String frame){
    return ((frame_data_size(frame)*8 + 48) != frame.length());
  }

  // Once an ACK is received, remove the ACK'd frame from frame queue
  private void received_ACK_for_frame(String frame){
    for(int i = 0; i < frame_list.size(); i++)
      if(frame_list.get(i).data.equals(frame_data(frame)))
        frame_list.remove(i);
  }

  // Alert the monitor that the node has no more frames to Tx
  private String alert_finished(String token){
    String binary_id = Frame.fill_bit_string(Integer.toBinaryString(id), 8);

    try{
      token = token.substring(0, token.length() - 10) + binary_id;
    }catch(StringIndexOutOfBoundsException e){
      token = Frame.fill_bit_string("", 39) + binary_id;
    }
    return token;
  }

  // Write frame to output-file-x
  protected void write_frame(String frame){

    try {
      writer.write(frame_source_address(frame) + "," +
              frame_destination_address(frame) + "," +
              frame_data_size(frame) + "," + frame_data(frame));

      writer.newLine();
      writer.flush();
    }catch(IOException e){
      System.exit(id);
      e.printStackTrace(System.out);
    }catch(NullPointerException e){
      print("NullPointer in write_frame");
      print(frame);
      if(writer == null) print("The writer is NULL");
      System.out.println(frame_destination_address(frame));
      e.printStackTrace();
      System.exit(id);
    }
  }

  // Parse frame for ACK
  // TRUE indicates ACK
  // FAlSE indicates NACK
  protected boolean frame_ACK(String frame){
    return((frame.substring(frame.length() - 2)).equals("10"));
  }

  // Parse frame for priority
  protected int frame_priority(String frame){
    return Integer.parseInt(frame.substring(0, 3), 2);
  }

  // Parse frame for source address
  protected int frame_source_address(String frame){
    try {
      return Integer.parseInt(frame.substring(24, 32), 2);
    }catch (NullPointerException e){
      print("NullPointer in frame_source_address");
      System.out.println(frame_destination_address(frame));
      System.exit(id);

    }

    return 0;
   }

  // Parse frame for destination address
  protected int frame_destination_address(String frame){
    return Integer.parseInt(frame.substring(16, 24), 2);
  }

  // Parse frame for data size
  protected  int frame_data_size(String frame){
    return Integer.parseInt(frame.substring(32, 40), 2);
  }

  // Parse frame for data
  protected String frame_data(String frame){
    String binary_data = frame.substring(40, 40 + (frame_data_size(frame)*8));
    String data = "";

    for(int i = 0; i < binary_data.length(); i += 8)
      data += (char)Integer.parseInt(binary_data.substring(i, i+8), 2);

    return data;
  }

  // Forward frame to right neighbor
  protected void transmit_frame(String frame) {
    // Ensure connection is open
    if(out_writer == null)
      open_output_stream();

    if(id > 0 && !alert_finished && is_token(frame)) {
      int finished_id = Integer.parseInt(frame.substring(frame.length() - 8), 2);
        if (finished_id == id) {
          frame = frame.substring(0, 4) + Frame.fill_bit_string("", 46);
          alert_finished = true;
        }
        // Alert the monitor that the node is finished transmitting
        else if (frame_list.isEmpty() && finished_id == 0) {

          frame = alert_finished(frame);
        }
    }

    out_writer.println(frame);
    out_writer.flush();
  }

  /*
  Transmits frames until the next frame to send + the sum of all previous
  bytes (for this transmit period) sent is <= the THT
   */
  private void transmit_state(){
    int bytes_sent = 0;
    int i = 0;

    while (!frame_list.isEmpty() && (i < frame_list.size()) &&
            ((bytes_sent + frame_list.get(i).frame_size()) <= Main.THT)){
      bytes_sent += frame_list.get(i).frame_size();

      transmit_frame(frame_list.get(i).to_binary());

      System.out.println("Transmitting frame in Node " + id + " for node " + frame_list.get(i).destination_address);
      i++;
    }
  }

  // Check if the frame is the token
  protected boolean is_token(String frame){
    return (frame.charAt(3) == '0');
  }

  //Open the socket output stream for writing to right neighbor
  private void open_output_stream(){
    System.out.println("Opening output stream in Node: " + id);
    out_writer = null;

    try{
      out_writer = new PrintWriter(neighbor_socket.getOutputStream(), true);
    } catch (IOException e){
      System.out.println("Error in Node " + id + " opening the output stream!\n");

      e.printStackTrace(System.out);
      System.exit(3);
    }
  }

  // Open socket input stream for listening to left neighbor
  protected void open_input_stream(){
    try{
      socket_reader = new BufferedReader(
              new InputStreamReader(client_socket.getInputStream()));
    }catch(IOException e){
      System.out.println("Error in Node " + id + "getting input stream.");
      e.printStackTrace(System.out);
      System.exit(id);
     }
  }

  // Open input-file-id for reading
  private void read_input_file(){
    try{
      input_file = new File("proj_files/input-file-" + id);
      input_file_reader = new BufferedReader(new FileReader(input_file));

    }catch(FileNotFoundException e){
      System.out.println("Error in Node " + id + "opening input-file-" +id);
      e.printStackTrace(System.out);
      System.exit(id);
    }

    read_frames();
  }

  // Read frames into frame_list
  private void read_frames(){
    String input, destination_address, data_size, data;
    String[] split_result;
    int i = 0;
    frame_list = new ArrayList<Frame>();

    try {
      while ((input = input_file_reader.readLine()) != null) {
        split_result = input.split(",");

        // Parse line
        destination_address = split_result[0];
        data_size = split_result[1];
        data = split_result[2];

        frame_list.add(i, new Frame(destination_address, data_size, data, id));
        i++;
      }
    }catch(IOException e){
      System.out.println("Error reading from input-file-"+id);
      e.printStackTrace(System.out);
    }
  }

  // Close all resources on exit
  public void finalize() throws Throwable{
    try{
      if(server_socket != null) server_socket.close();
    }catch(IOException e){ e.printStackTrace(System.out); }
    try{
      if(neighbor_socket != null) neighbor_socket.close();
    }catch(IOException e){ e.printStackTrace(System.out); }
    try{
      if(client_socket != null) client_socket.close();
    }catch(IOException e){ e.printStackTrace(System.out); }
    try{
      writer.close();
    }catch(IOException e){e.printStackTrace();}
  }

  // Open server socket for left neighbor
  public void open_server_socket(){
    try {
      this.server_socket = new ServerSocket(port_number);
    } catch (IOException e){
      System.out.println("Error creating a server socket in Node " +
        id + ", port number " + port_number + " is in use!\n");

      e.printStackTrace(System.out);
      System.exit(id);
    }
    if(id < 0) System.out.println("Ring #" + ring_num
            + " port number is: "
            + server_socket.getLocalPort());

    accept_clients();
  }

  //Accept left neighbor as a client
  private void accept_clients(){
    try{
      client_socket = server_socket.accept();
    } catch (IOException e){
      print("Error accepting Node " + (id - 1) +
        "as a client, port number " + port_number + " is in use!\n");

      e.printStackTrace(System.out);
      System.exit(id);
    }
  }

  //  Connect to right neighbor as a client
  public void connect_to_right_neighbor(){
    while(true){
      try {
        neighbor_socket = new Socket(Main.HOSTNAME, right_neighbor_port_number);

        if(neighbor_socket.isConnected()){
          break; // Break once connection is established
        }
      }catch(IOException e) {
        try{ // Try again if neighbor isn't accepting connections
          Thread.sleep(1000);
        }catch(InterruptedException err){
          e.printStackTrace(System.out);
        }
        continue;
      }
    }
  }

  protected void print(String line){
    System.out.println("Node " + id + ": " + line);
  }

  public void run(){}
}

