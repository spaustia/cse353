/*
Author: Steven Paustian
Assignment: Project 3
Class: CSE353
Due Date: 12/02/2014
Instructor: Dr. Soliman 
*/

#include "bridge.h"

// Initiate all sockets and variables
Bridge::Bridge(int ring1_port_num, int ring_2_port_number, char* conf_file){
  create_log_file(conf_file);

  BridgeSocket temp1_socket(ring1_port_num, 1);
  ring1_sockets = &temp1_socket;

  log_file << "Connected to Ring 1's monitor through port number: " << ring1_sockets->client_port_num << std::endl;
  log_file << "Accepted connection from Ring 1 through port number: " << ntohs(ring1_sockets->bridge_serv_addr.sin_port) << std::endl;

  BridgeSocket temp2_socket(ring_2_port_number, 2);
  ring2_sockets = &temp2_socket;

  log_file << "Connected to Ring 2's monitor through port number: " << ring2_sockets->client_port_num << std::endl;
  log_file << "Accepted connection from Ring 1 through port number: " << ntohs(ring2_sockets->bridge_serv_addr.sin_port)  << std::endl;

  ring1_frame = ring2_frame = false;

  clear_buffer(buffer);
  listen();
}

// Listen for frames from both rings
void Bridge::listen(){
  int n, m;
  n = m = 0;

  log_file << "Entering listen() state." << std::endl;

  char new_buffer[MAX_SIZE];
  int i = 0;

  fcntl(ring1_sockets->node_socket_fd, F_SETFL, O_NONBLOCK);
  fcntl(ring2_sockets->node_socket_fd, F_SETFL, O_NONBLOCK);

  while(true){
    clear_buffer(new_buffer);
    ring1_frame = ring2_frame = false;
    n = m = 0; 
    // print("Waiting for read to hit!");

    if( ((n = recv(ring1_sockets->node_socket_fd, new_buffer, MAX_SIZE, 0)) > 0) ||
        ((m = recv(ring2_sockets->node_socket_fd, new_buffer, MAX_SIZE, 0)) > 0)){

      print("INSIDE IF!");

        if( (n > 0) || (m > 0)){
            int buffer_size = 0;

            (n > 0 ? ring1_frame = true : ring2_frame = true);

            log_file << "\nReceived frame from Ring " << (ring1_frame ? "1" : "2") << ", parsing..." << std::endl;

            if(n < 0)
              log_file << "Error reading frame from ring 1.\n";
            if(m < 0)
              log_file << "Error reading frame from ring 2.\n";
            
            // Attempted to tokenize input as it is non-blocking
            //    but it creates an infinite loop.
            //    Keeping this as I have to turn something in.
            char* tokenized_string;
            tokenized_string = strtok(new_buffer, "\n");

              // Handle token
            if(is_token(tokenized_string)){
              log_file << "Frame recognized as Token." << std::endl;
              
              // Begin - Ugly attempt at handling finish of transmission
              if(ring1_frame && two_frames1){
                log_file << "Ring 1 has finished transmitting!" << std::endl;
                ring1_finished = true;
              }
              else if(ring2_frame && two_frames2){
                log_file << "Ring 2 has finished transmitting!" << std::endl;
                print("Ring 2 set to finished");
                ring2_finished = true;
              }

              (ring1_frame ? two_frames1 = true : two_frames2 = true);
              // End - Ugly

              transmit_state();

              if(ring1_frame && !ring1_finished)
                pass_token(tokenized_string);
              else if(ring2_frame && !ring2_finished)
                pass_token(tokenized_string);
            }
            else{
                // More transmission finish handling attempts
                if(ring1_frame){
                  two_frames1 = false;
                }
                else if(ring2_frame){
                  two_frames2 = false;
                }

                log_file << "Frame is not a token, handling..." << std::endl;
                update_id_maps(tokenized_string);
                store_frame(tokenized_string);
              }            
          }
        }//end read() if
  } // end while
}

// true == token is alerting it is finished
bool Bridge::is_token_finished(char* token){
  if((token[0]) == '1' && (token[1] = '1') && (token[2] == '1'))
    print("token has alerted finished!");
  return((token[0]) == '1' && (token[1] = '1') && (token[2] == '1'));
}

// Format for cstring compatibility
void Bridge::format_frame(char* frame){
  for(unsigned int i = 0; i < strlen(frame); i++){
    if(frame[i] == '\n'){
      frame[i+1] = '\0';
      break;
    }
  }
}

// Pass token to appropriate ring
void Bridge::pass_token(char* token){
  print("Passing the token!");
   token[strlen(token)] = '\n';
   token[strlen(token)] = '\0';
   int n;

    if( ring1_frame)
      n = send(ring1_sockets->monitor_socket_fd, token , strlen(token), 0);
    else
      n = send(ring2_sockets->monitor_socket_fd, token , strlen(token), 0);

    if(n < 0)
      print("Error passing on token to ring");
    else if(n == 0)
      print("Token queued for transmisison");
    else
      print("Successfull token transmission");
}

// Transmit queued frames upon token recepton
void Bridge::transmit_state(){
  int transmitted_bytes = 0;
  char *frame;

  log_file << "Entering transmit state for " << (ring1_frame ? "Ring 1." : "Ring 2.") << std::endl;

  if(ring1_frame){ // If the token is for ring 1
    while((ring1_queue.size() > 0)){ // Transmit as long as # bytes sent is < THT

      frame = strdup(ring1_queue[0]);
      
      if(in_vector(ring1_node_ids, dest_addr(frame))){
        log_file << "Destination recognized from mapping.  Forwarding to Ring 2." << std::endl;
        
        int n = send(ring1_sockets->monitor_socket_fd, frame, strlen(frame), 0);
        if(n < 0)
        print("Error sending frame to ring 1 in bridge!");
      }
      else if(in_vector(ring2_node_ids, dest_addr(frame))){
        log_file << "Destination recognized from mapping.  Forwarding to Ring 2." << std::endl;

        int n = send(ring2_sockets->monitor_socket_fd, frame, strlen(frame), 0);
        if(n < 0)
          print("Error sending frame to ring 2 in bridge!");
      }
      else{
        flood(frame);
      }
      
      transmitted_bytes += frame_size(frame);
      free(ring1_queue[0]); 
      ring1_queue.erase(ring1_queue.begin());
    }
  }
  else{
    while(transmitted_bytes < 5044 && (ring2_queue.size() > 0)){ // Transmit as long as # bytes sent is < THT

      frame = strdup(ring2_queue[0]);

      if(in_vector(ring1_node_ids, dest_addr(frame))){
         log_file << "Destination recognized from mapping.  Forwarding to Ring 1" << std::endl;
         int n = send(ring1_sockets->monitor_socket_fd, frame, strlen(frame), 0);
         if(n < 0)
          print("Error sending frame to ring 1 in bridge!");
      }
      else if(in_vector(ring2_node_ids, dest_addr(frame))){
        log_file << "Destination recognized from mapping.  Forwarding to Ring 2" << std::endl;

        int n = send(ring2_sockets->monitor_socket_fd, frame, strlen(frame), 0);
        if(n < 0)
          print("Error sending frame to ring 2 in bridge!");
      }
      else{
        flood(frame);
      }

      transmitted_bytes += frame_size(frame);
      free(ring2_queue[0]); 
      ring2_queue.erase(ring2_queue.begin());
    }
  }
}

// Send frame to both rings
void Bridge::flood(char* frame){
  int n = send(ring1_sockets->monitor_socket_fd, frame, strlen(frame), 0);
  int m = send(ring2_sockets->monitor_socket_fd, frame, strlen(frame), 0);

  log_file << "Destination not recognized.  Flooding..." << std::endl;

  
  if(n < 0){
    print("Error sending frame to ring 1 in flood!");
  }
  else if(m <  0){
    print("Error sending frame to ring 2 in flood!");
  }
}

// Map the source address based on the ring it came from
void Bridge::update_id_maps(char *frame){
  //  Do nothing if the src_addr is already  mapped
  if(in_vector(ring1_node_ids, src_addr(frame)) || in_vector(ring2_node_ids, src_addr(frame))){
    log_file << "Source address is already mapped!" << std::endl;
  }
  // Map src_addr to ring1 or ring2
  else if(ring1_frame){
    log_file << "Mapping new source address " << src_addr(frame) << " to Ring 1."  << std::endl;

    ring1_node_ids.push_back(src_addr(frame));
  }
  else{
    log_file << "Mapping new source address " << src_addr(frame) << " to Ring 2."  << std::endl;
   
    ring2_node_ids.push_back(src_addr(frame));
  }
}

// true == token
// false == frame
bool Bridge::is_token(char* input){
  return(input[3] == '0');
}

// Return frame source address
int Bridge::src_addr(char *input){
  int addr = 0;

  for(int i = 0; i < 8; i++){
    if(input[31-i] == '1'){
      addr += (pow(2,i));
    }
  }
    return addr;
}

// Returns frame destination address
int Bridge::dest_addr(char* input){
  int addr = 0;
  for(int i = 0; i < 8; i++){
    if(input[23-i] == '1'){
      addr += (pow(2,i));
    }
  }
  return addr;
}

// true == subnet alerting bridge it is finished
// false == just a regular token
bool Bridge::alert_finished(){
  return false;
}

// Adds received frame to queue
void Bridge::store_frame(char* frame){
  char* input1 = strdup(frame);
  char* input2 = strdup(frame);

  set_ACK(input1);
  set_ACK(input2);

  input1[strlen(input1)] = '\n';
  input1[strlen(input1)] = '\0';
  input2[strlen(input2)] = '\n';
  input2[strlen(input2)] = '\0';
  
  // Push to appropriate queue when dest_addr and src_addr is mapped
  if(in_vector(ring1_node_ids, dest_addr(input1)) && in_vector(ring1_node_ids, src_addr(input1))){
    log_file << "Queueing frame in for Ring 1."  << std::endl;
    ring1_queue.push_back(input1);
  }
  else if(in_vector(ring2_node_ids, dest_addr(input2)) && in_vector(ring2_node_ids, src_addr(input2))){
    log_file << "Queueing frame in for Ring 2."  << std::endl;

    ring2_queue.push_back(input2);
  }
  else{

  ring1_queue.push_back(input1);
  ring2_queue.push_back(input2);
  }
}

// Set ACK if no ACK, and store to be sent
void Bridge::set_ACK(char *frame){
  if(!has_ACK(frame)){
    print("Setting ACK");
    int buffer_end = strlen(frame) - 1;
    frame[buffer_end - 1] = '1';
  }
}

//  Returns where the frame has an ACK
bool Bridge::has_ACK(char* frame){
  int buffer_end = strlen(frame) - 1;

  if(frame[buffer_end-1] == '1') return true;

  return false;
}

// Easy print function
void Bridge::print(std::string line){
  std::string ring =
    (ring1_frame ? "Ring_1" : "Ring_2");
}

// Print char*
void Bridge::print(char* buffer){
  std::string convert(buffer);
  print(convert);
}

// More print help
void Bridge::print(char letter){
    std::string ring =
      (ring1_frame ? "Ring_1" : "Ring_2");
  std::cout << "Bridge::" << ring << ": " << letter << std::endl;
}

// Convert int to string for printing
std::string Bridge::int_to_str(int num){
  std::ostringstream convert;   // stream used for the conversion
  convert << num;      // insert the textual representation of 'Number' in the characters in the stream
  return convert.str();
}

// Clear passed cstring
void Bridge::clear_buffer(char* buffer){
  for(unsigned int i = 0; i < MAX_SIZE; i++){
    buffer[i] = 0;
  }
}

// Returns whether the element argument is a member of the vector argument
//    Used to determine if the dest_addr is inside the list of known ids
bool Bridge::in_vector(std::vector<int> elements_vector, int element){
  for(unsigned int i = 0; i < elements_vector.size(); i++)
    if(elements_vector[i] == element) return true;

  return false;
}

// Return frame size in BITS
int Bridge::frame_size(char* frame){
  int size = 48;
  size += (data_size(frame) * 8);
  return size;
}

// Returns data size as int
int Bridge::data_size(char* frame){
  int size = 0;

  for(int i = 0; i < 8; i++){
    if(buffer[(int)(39-i)] == '1'){
      size += (pow(2,i));
    }
  }
    return size;
}

// Open log file for writing
void Bridge::create_log_file(char* conf_file){
  log_file.open(conf_file);
}

// Destructor
Bridge::~Bridge(){
  //Don't handle socket closure here, that happens in the BridgeSocket destructor
  log_file.close();

  log_file << "Both nodes have alerted they are finished transmitting.\nClosing all sockets and exiting..."  << std::endl;
}