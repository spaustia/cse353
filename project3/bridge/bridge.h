/*
Author: Steven Paustian
Assignment: Project 3
Class: CSE353
Due Date: 12/02/2014
Instructor: Dr. Soliman 
*/

#ifndef BRIDGE
#define BRIDGE

#include <array>
#include <queue>
#include <string>
#include <sstream>
#include <math.h>
#include <vector>
#include <fcntl.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <fstream>

#include "bridge_socket.h"

#define MAX_SIZE 2424

class Bridge{
  private:
    BridgeSocket* ring1_sockets;
    BridgeSocket* ring2_sockets;

    std::vector<int> ring1_node_ids, ring2_node_ids;
    char buffer[MAX_SIZE];

    bool ring1_frame, ring2_frame;
    bool ring1_finished;
    bool ring2_finished;

    std::vector<char *> ring1_queue;
    std::vector<char*> ring2_queue;

    // Methods
    void listen();
    void write_ring1();
    void write_ring2();
    void flood_rings();
    void transmit_state();
    void write_frame();
    void flood(char*);
    void store_frame(char*);
    void update_id_maps(char*);
    void set_ACK(char *);
    void format_frame(char*);

    // Print help for debugging
    void print(std::string);
    void print(char*);
    void print(char);
    
    void clear_buffer(char *);
    void pass_token(char*);

    bool alert_finished();
    bool is_token(char*);
    bool in_vector(std::vector<int>, int);
    bool has_ACK(char*);
    bool is_token_finished(char*);

    int src_addr(char*);
    int dest_addr(char*);
    int frame_size(char*);
    int data_size(char*);

    std::string int_to_str(int);

    // Ugly - ignore
    bool two_frames1;
    bool two_frames2;

    // For select() on sockets
    fd_set readfds;
    struct timeval tv;

    //Log file methods and members
    std::ofstream log_file;
    void create_log_file(char*);

  public:
    Bridge(int, int, char*);
    ~Bridge();
};

#endif