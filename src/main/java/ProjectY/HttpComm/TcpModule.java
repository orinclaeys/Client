package ProjectY.HttpComm;

import ProjectY.Client.ClientApplication;
import ProjectY.Client.FileLog;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class TcpModule implements Runnable{
    private static DataInputStream dataInputStream = null;
    private static DataOutputStream dataOutputStream = null;
    public String Filename;
    public int portnumber;

    public TcpModule() {}

    public void sendFile(int ownerID, String ownerIP,String ReplicatorIP, String filename){
        if (ReplicatorIP!=null) {
            try {
                HttpModule httpModule = new HttpModule();
                int portNumber = 5005;
                JSONObject message = new JSONObject();
                message.put("ownerID",ownerID);
                message.put("ownerIP",ownerIP);
                message.put("DestinationAddress", ReplicatorIP);
                message.put("PortNumber", portNumber);
                message.put("Filename", filename);
                httpModule.sendFileInformation(ReplicatorIP, message);

                Socket socket = new Socket(ReplicatorIP, portNumber);

                OutputStream outputStream = socket.getOutputStream();

                FileInputStream fileInputStream = new FileInputStream("src/main/java/ProjectY/Client/Files/" + filename);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                fileInputStream.close();
                outputStream.close();
                socket.close();

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
   }
    public void run(){
        try {
            boolean end=false;
            ServerSocket serverSocket = new ServerSocket(portnumber);

            while (!end){
                Socket clientSocket = serverSocket.accept();
                InputStream inputStream = clientSocket.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream("src/main/java/ProjectY/Client/Files/" + Filename);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer,0,bytesRead);
                }

                fileOutputStream.close();
                inputStream.close();
                serverSocket.close();
                end=true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
   }
}
