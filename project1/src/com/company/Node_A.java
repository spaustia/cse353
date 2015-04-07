package com.company;

import java.io.*;
import java.net.Socket;

/**
 * Node_A will wait for Node_B to open a server, then connect
 * and send the data in confA.txt, minus the initial port number.
 */

public class Node_A implements Runnable{

    private File confA_file;
    private BufferedReader file_reader;
    private Socket client_socket;
    private PrintWriter out;

    private int node_ab_port;


    public Node_A(){
        //Open conf and populate port numbers
        try {
            confA_file = new File("confA.txt");
            file_reader = new BufferedReader(new FileReader(confA_file));
            node_ab_port = Integer.parseInt(file_reader.readLine());

            //Connect and send data from confA
            open_client_socket();
            open_output_stream();
            send_data();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        finally{ //Close attributes
            try{
                if(file_reader != null) file_reader.close();
            }catch(IOException e){}
            try{
                if(client_socket != null) client_socket.close();
                if(out != null) out.close();
            }catch(IOException e){}
        }
    }

    //Open a socket to connect to Node_B
    private void open_client_socket() {
        client_socket = null;

        while (true) {
            if (Main.node_B_ready_for_A) { //Wait until Node_B is ready
                try {
                    client_socket = new Socket(Main.HOSTNAME, node_ab_port);
                } catch (IOException e) {
                    System.out.println("Error connecting to Node_A as a client, port number " + node_ab_port + " is in use!\n");
                    e.printStackTrace(System.out);
                    System.exit(3);
                }
                return;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    //Open the socket output stream for writing
    private void open_output_stream(){
        out = null;

        try{
            out = new PrintWriter(client_socket.getOutputStream(), true);
        } catch (IOException e){
            System.out.println("Error opening the output stream to Node_B\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }

    //Send data to Node_B
    private int send_data(){
        String input;

        try{
            while((input = file_reader.readLine()) != null){
                out.println(input);
            }
            out.println("terminate");
        } catch (IOException e){
            System.out.println("Error sending Node_A data\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
        Main.node_A_done = true;
        return 0;
    }

    public void run(){}
}
