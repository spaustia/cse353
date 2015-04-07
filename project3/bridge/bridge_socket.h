/*
Author: Steven Paustian
Assignment: Project 3
Class: CSE353
Due Date: 12/02/2014
Instructor: Dr. Soliman 
*/

/*
    This BridgeSocket class provides socket connection compartmentalization.
    The sockets which send and receive data to the token rings are housed here.

    Order of operations is as follows:
        1) Connect to the monitor node.
        2) Pass the monitor the port number for node connection to the bridge.
            - The monitor will then communicate this number to the node via a special frame.
        3) Accept the node's connection.
        4) Profit
*/

#ifndef BRIDGE_SOCKET
#define BRIDGE_SOCKET

#include <iostream>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <string.h>

class BridgeSocket{
  private:
    // Client socket vars
    struct sockaddr_in client_serv_addr;
    struct hostent *client_server;

    // Server socket vars
    int bridge_socket_fd;
    struct sockaddr_in client_addr;
    struct hostent *bridge_server;

    // Ring number
    int ring_num;

    // Holds port num to be sent to monitor
    char buffer[2080];

    void connect_as_client();
    void init_server_socket();
    void accept_ring_connection();

  public:
    //  For access by the bridge
    int monitor_socket_fd, node_socket_fd;
    struct sockaddr_in bridge_serv_addr;
    int client_port_num;
    int bridge_port_num;

    // Take the monitor's port number as it's first argument
    //      and the ring number (1, 2) as the second argument
    BridgeSocket(int, int);

    // Close sockets
    ~BridgeSocket();
};

#endif