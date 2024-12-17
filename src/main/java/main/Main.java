package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

     ServerSocket serverSocket;
     Socket clientSocket = null;
     int port = 9092;
     try {
       serverSocket = new ServerSocket(port);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       // Wait for connection from client.
       clientSocket = serverSocket.accept();
     DataInputStream in = new DataInputStream(clientSocket.getInputStream());
     OutputStream out = clientSocket.getOutputStream();

     while (true){
        handleRequests(in, out);
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

    private static void handleRequests(DataInputStream in, OutputStream out) throws IOException {
        int incomingMessageSize = in.readInt(); //size
        byte[] requestApiKeyBytes = in.readNBytes(2); // api key
        short apiVersion =in.readShort();
        byte[] corrId = in.readNBytes(4);
        byte[] remainingBytes = new byte[incomingMessageSize - 8];
        in.readFully(remainingBytes);

        var bos = new ByteArrayOutputStream();
        bos.write(corrId);

        bos.write(getErrorCode(apiVersion));
        bos.write(2);                       // array size + 1
        bos.write(requestApiKeyBytes);      // api_key
        bos.write(new byte[] {0, 0});       // min version
        bos.write(new byte[] {0, 4});       // max version
        bos.write(0);                       // tagged fields
        bos.write(new byte[] {0, 0, 0, 0}); // throttle time
        // All requests and responses will end with a tagged field buffer.  If
        // there are no tagged fields, this will only be a single zero byte.
        bos.write(0); // tagged fields
        int size = bos.size();
        byte[] sizeBytes = ByteBuffer.allocate(4).putInt(size).array();
        var response = bos.toByteArray();
        System.out.println("response size: " + Arrays.toString(sizeBytes));
        System.out.println("response: " + Arrays.toString(response));
        out.write(sizeBytes);
        out.write(response);
        //out.flush();
    }

    static byte[] getErrorCode(short apiVersion){
        if (apiVersion < 0 || apiVersion > 4) {
            // error code 16bit
            return new byte[] {0, 35};
        }
        return (new byte[] {0, 0});
    }
}
