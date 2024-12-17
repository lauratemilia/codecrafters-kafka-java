package main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

     ServerSocket serverSocket = null;
     Socket clientSocket = null;
     int port = 9092;
     try ( InputStream in = clientSocket.getInputStream();
           OutputStream out = clientSocket.getOutputStream();) {
       serverSocket = new ServerSocket(port);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       // Wait for connection from client.
       clientSocket = serverSocket.accept();


       in.readNBytes(4);
       byte[] apiKey = in.readNBytes(2);
       byte[] apiVersion = in.readNBytes(2);
       short shortApiVersion = ByteBuffer.wrap(apiVersion).getShort();
       System.out.println(shortApiVersion);
       byte[] corrId = in.readNBytes(4);
     var bos = new ByteArrayOutputStream();
     bos.write(corrId);

     if (shortApiVersion < 0 || shortApiVersion > 4) {
         // error code 16bit
         bos.write(new byte[] {0, 35});
     } else {
         // error code 16bit
         //    api_key => INT16
         //    min_version => INT16
         //    max_version => INT16
         //  throttle_time_ms => INT32
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
     System.out.println(Arrays.toString(sizeBytes));
     System.out.println(Arrays.toString(response));
     out.write(sizeBytes);
     out.write(response);
     out.flush();
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
