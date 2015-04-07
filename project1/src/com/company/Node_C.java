package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Node_C will open a server to allow connections from Node_B, and signal it is ready.
 * It will then print the data received and exit.
 */

public class Node_C extends Thread{
    private File file;
    private BufferedReader file_reader;
    private ServerSocket server_socket;
    private Socket client_socket;
    private BufferedReader socket_reader;
    private int node_bc_port;

    public Node_C(){
        //Open conf and populate port number
        try {
            file = new File("confC.txt");
            file_reader = new BufferedReader(new FileReader(file));
            node_bc_port = Integer.parseInt(file_reader.readLine());

            //Open a server, accept Node_B as a client, the read data
            open_server_socket();
            accept_clients();
            read_data();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        finally{ //Close all attributes
            try{
                if(file_reader != null){ file_reader.close(); }
            }catch(IOException e){}
            try{
                if(socket_reader != null) socket_reader.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
            try{
                if(server_socket != null) server_socket.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
            try{
                if(client_socket != null) client_socket.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
        }
    }

    private void open_server_socket(){
        try {
            this.server_socket = new ServerSocket(node_bc_port);
        } catch (IOException e){
            System.out.println("Error creating a server socket in Node_C, port number " + node_bc_port + " is in use!\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }

    //Accept connections from Node_B
    private void accept_clients(){
        try{
            Main.node_C_ready_for_B = true;
            client_socket = server_socket.accept();
        } catch (IOException e){
            System.out.println("Error accepting Node_B as a client, port number " + node_bc_port + " is in use!\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }

    //Read data from Node_B
    private void read_data(){
        String input;
        try{
            socket_reader = new BufferedReader(
                    new InputStreamReader(client_socket.getInputStream()));
        }catch(IOException e){
            e.printStackTrace(System.out);
        }

        try {
            while ((input = socket_reader.readLine()) != null) {
                if(input.equals("terminate")) System.exit(0); //Exit
                System.out.println("Node C received: " + input);
            }
        } catch(IOException e){
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }
    public void run(){}
}
