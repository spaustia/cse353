
/*
Author: Steven Paustian
Date: 9/15/14
Course: CS353
Assignment: Project 1
Due: 9/21/14
 */

package com.company;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Thread;

/*
Main will create 3 threads which start the nodes.  The threads are added to a
List, shuffled, then started in random order.
 */

public class Main{

    //Default Hostname
    public static final String HOSTNAME = "127.0.0.1";
    //For signaling
    public static Boolean node_A_done = false;
    public static Boolean node_B_ready_for_A = false;
    public static Boolean node_C_ready_for_B = false;

    public static void main(String[] args) {

        //Create a list to hold all the threads
        List<Thread> node_threads = new ArrayList();

        //Add the threads that will start the nodes
        node_threads.add(0, new Thread() {
            public void run() {
                new Node_A();
                return;
            }
        });

        node_threads.add(1, new Thread() {
            public void run() {
                new Node_B();
                return;
            }
        });

        node_threads.add(2, new Thread() {
            public void run() {
                new Node_C();
                return;
            }
        });

        //Shuffle the collection for random start
        Collections.shuffle(node_threads);

        //Start the shuffled threads
        for(int i = 0; i < node_threads.size(); i++){
            node_threads.get(i).start();

        }
    }

}
