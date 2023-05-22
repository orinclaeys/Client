package ProjectY.HttpComm;

import ProjectY.Client.ClientApplication;
import ProjectY.Client.FileLog;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class TcpModule {
    private static DataInputStream dataInputStream = null;
    private static DataOutputStream dataOutputStream = null;
    public void replicateFile(FileLog fileLog) {
        System.out.println("Replicate file");
    }

    public TcpModule() {
    }

    public void sendFile(String ownerIP,String ReplicatorIP, String filename){
        try {
            System.out.println("TCP: " + ownerIP + " Filename: " + filename);

            HttpModule httpModule = new HttpModule();
            int portNumber = 5005;
            JSONObject message = new JSONObject();
            message.put("DestinationAddress",ReplicatorIP);
            message.put("PortNumber",portNumber);
            message.put("Filename", filename);
            httpModule.sendFileInformation(ownerIP,message);

            //Socket socket = new Socket(destinationIP,portNumber);
            Socket socket = new Socket(ClientApplication.client.getIPAddres(), portNumber);
            System.out.println("CLIENT: connected to server. Sending file...");

            OutputStream outputStream = socket.getOutputStream();

            FileInputStream fileInputStream = new FileInputStream("src/main/java/ProjectY/Client/Files/" + filename);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,bytesRead);
            }

            System.out.println("CLIENT: File sent");

            fileInputStream.close();
            outputStream.close();
            socket.close();



        }catch (Exception e){
            System.out.println(e.toString());
        }

   }

   public void receiveFile(int portNumber, String filename){
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);

            System.out.println("SERVER: Listening on port " + portNumber);

            while (true){
                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                InputStream inputStream = clientSocket.getInputStream();

                FileOutputStream fileOutputStream = new FileOutputStream("src/main/java/ProjectY/Client/Files/replicas/" + filename);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer,0,bytesRead);
                }

                System.out.println("File received and saved");

                fileOutputStream.close();
                inputStream.close();
                serverSocket.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

   }


}
