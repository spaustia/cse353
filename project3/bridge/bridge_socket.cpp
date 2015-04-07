/*
Author: Steven Paustian
Assignment: Project 3
Class: CSE353
Due Date: 12/02/2014
Instructor: Dr. Soliman 
*/

#include "bridge_socket.h"
#include <unistd.h>

// BridgeSocket constructor, its arguments are the
// port number we will connect to and the ring number 
// for ring identification
BridgeSocket::BridgeSocket(int client_port_num, int ring_num){
  this->client_port_num = client_port_num;
  this->ring_num = ring_num;

  init_server_socket();
}

// Connect to the client_port_num
void BridgeSocket::connect_as_client(){
  // Create socket
  monitor_socket_fd = socket(AF_INET, SOCK_STREAM, 0);
  if(monitor_socket_fd < 0){
    std::cout << "Error creating socket in bricket_socket " << ring_num;
    std::cout << ". Exiting..." << std::endl;
    exit(-1);
  }

  // Ensure client exists
  client_server = gethostbyname("127.0.0.1");
  if(client_server == NULL){
    std::cout << "No client_server known by name 127.0.0.1." ;
    std::cout << " in BridgeSocket " << ring_num << "Exiting..." << std::endl;
    exit(-1);
  }

  bzero((char *) &client_serv_addr, sizeof(client_serv_addr));
  client_serv_addr.sin_family = AF_INET; // Use TCP in the Internet domain
  
  bcopy((char *)client_server->h_addr, 
       (char *)&client_serv_addr.sin_addr.s_addr,
       client_server->h_length);

  client_serv_addr.sin_port = htons(client_port_num);

  //  Connect to monitor
  if (connect(monitor_socket_fd,(struct sockaddr *) &client_serv_addr, sizeof(client_serv_addr)) < 0) {
    std::cout << "Error connection to ring " << ring_num << ". Exiting..." << std::endl;
    exit(-1);
  }
  
  char temp[20];
  for(int i = 0; i < 20; i++)
    temp[i] = 0;
  for(int i = 0; i < 2080; i++){
    buffer[i] = 0;
  }

  sprintf(temp, "%u", ntohs(bridge_serv_addr.sin_port));
  strcpy(buffer, temp);
  buffer[strlen(buffer)] = '\n';
  buffer[strlen(buffer)] = '\0';

  //  Send monitor port num for node to connect to
  int n = send(monitor_socket_fd, buffer, strlen(buffer), MSG_DONTWAIT);
  fsync(monitor_socket_fd);

  if(n < 0)
    std::cout << "Error writing test message to socket" << std::endl;

  accept_ring_connection();
}

// Sets up the server_socket that the ring will connect to.
//  We have to set this up first so we can pass our port number
//  to the ring.  listen() DOES NOT get called here, as it halts the system.
void BridgeSocket::init_server_socket(){
  bridge_socket_fd = socket(AF_INET, SOCK_STREAM, 0);

  if(bridge_socket_fd < 0){
    std::cout << "ERROR opening bridge server socket!." << std::endl;
    exit(-1);
  }

  bzero((char *) &bridge_serv_addr, sizeof(bridge_serv_addr));
  bridge_serv_addr.sin_family = AF_INET;
  bridge_serv_addr.sin_addr.s_addr = INADDR_ANY;
  bridge_serv_addr.sin_port = htons(2000);

  // Starting at port num 2000, check for availability.
  //  If bind call fails, check the next port number
  //  until bind doesn't fail.
  int i = 2000;
  while(true){
    if (bind(bridge_socket_fd, (struct sockaddr *) 
               &bridge_serv_addr,
               sizeof(bridge_serv_addr))
         < 0){
      i++;
      bridge_serv_addr.sin_port = i;
    }
    else break;
  }
  connect_as_client();
}

// Now that we have connected to the ring as a client,
// and passed on our port number it to connection,
// we can start listening for a connection.
void BridgeSocket::accept_ring_connection(){
  listen(bridge_socket_fd, SOMAXCONN);

  node_socket_fd = accept(bridge_socket_fd, NULL, NULL);

  if(node_socket_fd < 0){
    std::cout << "Error on accept in bridge socket.  Exiting..." << std::endl;
    exit(-1);
  }
}

// Close all sockets on exit
BridgeSocket::~BridgeSocket(){
  close(bridge_socket_fd);
  close(monitor_socket_fd);
  close(node_socket_fd);
}