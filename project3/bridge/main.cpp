/*
Author: Steven Paustian
Assignment: Project 3
Class: CSE353
Due Date: 12/02/2014
Instructor: Dr. Soliman 
*/

#include <iostream>
#include "bridge_socket.h"
#include "bridge.h"

void check_args(int, char**);
void usage();

int main(int argc, char* argv[]){
  check_args(argc, argv);

  // Port numbers
  int ring_1_port_num, ring_2_port_num;

  // Get ring port numbers for connection
  std::cin.clear();

  std::cout << "Please enter ring 1's port number: " << std::endl;
  std::cin >> ring_1_port_num;
  std::cout << "Please enter ring 2's port number: " << std::endl;
  std::cin >> ring_2_port_num;
  std::cout << "Thanks!  Starting bridge...\n";

  // Create the bridge
  Bridge bridge(ring_1_port_num, ring_2_port_num, argv[1]);

  return 0;
}

// Ensure only one argument is passed
void check_args(int argc, char* argv[]){
  if(argc != 2 || argv[0] == NULL || argv[1] == NULL)
    usage();
}

// Display correct usage
void usage(){
  std::cout << "Bad arguments detected!" << std::endl;
  std::cout << "Usage: " << std::endl;
  std::cout << "./Bridge file.log" << std::endl;
  exit(-1);
}