package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

     ServerSocket serverSocket = null;
     Socket clientSocket = null;
     int port = 9092;
     try {
       serverSocket = new ServerSocket(port);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       // Wait for connection from client.
       clientSocket = serverSocket.accept();
       InputStream in = clientSocket.getInputStream();
       OutputStream out = clientSocket.getOutputStream();

       byte[] length = in.readNBytes(4);
       byte[] apiKey = in.readNBytes(2);
       byte[] apiVersion = in.readNBytes(2);
       short shortApiVersion = ByteBuffer.wrap(apiVersion).getShort();
       System.out.println(shortApiVersion);
       byte[] corrId = in.readNBytes(4);
       out.write(length);
       out.write(corrId);
       if(shortApiVersion < 0 || shortApiVersion > 4){
           out.write(new byte[]{0,35});
       }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     } finally {
       try {
         if (clientSocket != null) {
           clientSocket.close();
         }
       } catch (IOException e) {
         System.out.println("IOException: " + e.getMessage());
       }
     }
  }
}
