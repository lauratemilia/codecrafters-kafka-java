package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

    private static void handleRequests(InputStream in, OutputStream out) throws IOException {
        in.readNBytes(4); //size
        in.readNBytes(2); // api key
        byte[] apiVersionBytes = in.readNBytes(2);
        short apiVersion = ByteBuffer.wrap(apiVersionBytes).getShort();
        byte[] corrId = in.readNBytes(4);
        var bos = new ByteArrayOutputStream();
        bos.write(corrId);

        if (apiVersion < 0 || apiVersion > 4) {
            // error code 16bit
            bos.write(new byte[] {0, 35});
        } else {
            bos.write(new byte[] {0, 0});       // error code
            bos.write(2);                       // array size + 1
            bos.write(new byte[] {0, 18});      // api_key
            bos.write(new byte[] {0, 3});       // min version
            bos.write(new byte[] {0, 4});       // max version
            bos.write(0);                       // tagged fields
            bos.write(new byte[] {0, 0, 0, 0}); // throttle time
            // All requests and responses will end with a tagged field buffer.  If
            // there are no tagged fields, this will only be a single zero byte.
            bos.write(0); // tagged fields
        }
        int size = bos.size();
        byte[] sizeBytes = ByteBuffer.allocate(4).putInt(size).array();
        var response = bos.toByteArray();
        System.out.println("response size: " + Arrays.toString(sizeBytes));
        System.out.println("response: " + Arrays.toString(response));
        out.write(sizeBytes);
        out.write(response);
        out.flush();
    }
}
