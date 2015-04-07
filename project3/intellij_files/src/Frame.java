import java.io.Console;

/**
 * Steven Paustian
 * CS353
 * Project 2
 * 11/04/2014
 */

/*
  This class converts the information read in from input-file-x
  to a binary frame.  It also can create a token
*/
  
public class Frame {
  public String destination_address, data_size, data;
  public int source_address;

  // Binary strings
  private String PPP, T, M, RRR, FC, DA, SA, SIZE, DATA, FS;

  public boolean sent, ACK;

  // Create frame from input-file-x
  public Frame(String destination_address,
               String data_size, String data,
               int source_address){
    this.destination_address = destination_address;
    this.data_size = data_size;
    this.data = data;
    this.source_address = source_address;
    sent = ACK = false;

    create_binary_frame();
  }

  // Create token frame
  public Frame(boolean token){
    //Tokens are min frame size with all 0's
    if(token){
      PPP = RRR ="000";
      T = M = DATA = "0";
      FC = DA = SA = SIZE = FS = "00000000";
    }
  }

  // Set bit-string representation of frame from input-file-x
  private void create_binary_frame(){
    PPP = "000";
    T = "1";
    M = "0";
    RRR = "000";

    FC = "00000001";
    DA = fill_bit_string(Integer.toBinaryString(Integer.parseInt(destination_address)), 8);
    SA = fill_bit_string(Integer.toBinaryString(source_address), 8);
    SIZE = fill_bit_string(Integer.toBinaryString(Integer.parseInt(data_size)), 8);

    byte[] bytes = data.getBytes();
    StringBuilder binary = new StringBuilder();
    for (byte b : bytes){
      int val = b;

      for (int i = 0; i < 8; i++){
        binary.append((val & 128) == 0 ? 0 : 1);
        val <<= 1;
      }
    }

    DATA = binary.toString();
    FS = "00000000";
  }

  public void print(){
    System.out.println("PPP" + ": " + PPP);
    System.out.println("T" + ": " + T);
    System.out.println("M" + ": " + M);
    System.out.println("RRR" + ": " + RRR);
    System.out.println("FC" + ": " + FC);
    System.out.println("DA" + ": " + DA);
    System.out.println("SA" + ": " + SA);
    System.out.println("SIZE" + ": " + SIZE);
    System.out.println("DATA length, data: " + DATA.length() + " " + DATA);
    System.out.println("FS" + ": " + FS);
  }

  // Static function to fill out a string with specified count of bits, in 0's
  public static String fill_bit_string(String string, int num_bits) {
    if (string.length() < num_bits)
      for (int i = 0, j = num_bits - string.length(); i < j; i++)
        string = "0" + string;

    return string;
  }

  // Convert frame to binary string
  public String to_binary(){
    return (PPP+T+M+RRR+FC+DA+SA+SIZE+DATA+FS);
  }

  // Garbled frames don't have a FS byte
  public String garbled_binary_frame(){
    return (PPP+T+M+RRR+FC+DA+SA+SIZE+DATA);
  }

  // Size of frame in bytes
  public int frame_size(){
    return to_binary().length();
  }
}