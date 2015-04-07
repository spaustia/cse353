package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
 /*
 Node_B will open a server to accept a connection from Node_A.
 It will then connect to Node_C, when its ready and send its data.
  */

public class Node_B implements Runnable{

    private File confB_file;
    private BufferedReader file_reader;
    private BufferedReader socket_reader;
    private ServerSocket server_socket;
    private PrintWriter out_writer;
    private Socket client_socket;

    //Holds port numbers
    private int node_ab_port;
    private int node_bc_port;

    public Node_B(){
        //Open confB file and populate port numbers
        try {
            confB_file = new File("confB.txt");
            file_reader = new BufferedReader(new FileReader(confB_file));

            node_ab_port = Integer.parseInt(file_reader.readLine());
            node_bc_port = Integer.parseInt(file_reader.readLine());

            //Create server, accept Node_A as a client then read the data
            open_server_socket();
            accept_clients();
            read_data();

            //Connect to Node_C, send data
            while(true) {
                if (Main.node_A_done & Main.node_C_ready_for_B) {
                    open_client_socket();
                    open_output_stream();
                    send_data();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }


        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        finally{ //Close all closeable attributes
            try{
                if(file_reader != null) file_reader.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
            try{
                if(socket_reader != null) socket_reader.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
            try{
                if(out_writer != null) server_socket.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
            try{
                if(server_socket != null) server_socket.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
            try{
                if(client_socket != null) client_socket.close();
            }catch(IOException e){ e.printStackTrace(System.out); }
        }
    }

    //Open a server for Node_A to connect too
    private void open_server_socket(){
        try {
            this.server_socket = new ServerSocket(node_ab_port);
        } catch (IOException e){
            System.out.println("Error opening server in Node_B, port number" + node_ab_port + " is in use!\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }

    //Accept connections from Node_A
    private void accept_clients(){
        try{
            Main.node_B_ready_for_A = true;
            client_socket = server_socket.accept();
        } catch (IOException e){
            System.out.println("Error accepting Node_B as a client, port number" + node_ab_port + " is in use!\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }

    //Read data from node_A
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
                if (input.equals("terminate")) return;

                System.out.println("Node B received: " + input);
            }
        } catch(IOException e){
            e.printStackTrace(System.out);
            System.exit(3);
        }
    }


    //Open a socket to connect to Node_C
    private void open_client_socket() {
        client_socket = null;

        while (true) {
            if (Main.node_C_ready_for_B) { //Wait until Node_B is ready
                try {
                    client_socket = new Socket(Main.HOSTNAME, node_bc_port); //Reuse existing socket
                } catch (IOException e) {
                    System.out.println("Error connecting to Node_C as a client, port number" + node_ab_port + " is in use!\n");
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
        out_writer = null;

        try{
            out_writer = new PrintWriter(client_socket.getOutputStream(), true);
        } catch (IOException e){
            System.out.println("Error in Node_A opening the output stream to Node_C!\n");

            e.printStackTrace(System.out);
            System.exit(3);
        }
    }

    //Send data to Node_C
    private int send_data(){
        String input;

        try{
            while((input = file_reader.readLine()) != null){
                out_writer.println(input);
            }
            out_writer.println("terminate");
        } catch (IOException e){
            System.out.println("Error sending data to Node_C\n");
            e.printStackTrace(System.out);
            System.exit(3);
        }
        return 0;
    }

    public void run(){}
}
