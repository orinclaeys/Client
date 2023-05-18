package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static java.lang.Math.abs;

public class Client {
    private int previousID;
    private int nextID;
    private int currentID;
    private String name;
    private String IPAddres;
    public boolean firstNode=false;
    private HttpModule httpModule = new HttpModule();
    private TcpModule tcpModule = new TcpModule();
    public static String ServerIP = "172.30.0.5";
    private Vector<FileLog> fileLogList = new Vector<>();


    public Client() {
        System.out.println("Enter name: ");
        this.name = "test";
        this.name = System.console().readLine();
        System.out.println("Enter IP-Address: ");
        this.IPAddres = "192.168.1.2";
        this.IPAddres = System.console().readLine();
        this.currentID = Hash(this.name);
        this.previousID = this.currentID;
        this.nextID = this.currentID;
        //Discovery();
        //verifyFiles();
    }

    public void initialize(){
        Discovery();
    }

    public boolean updateNextID(String name){
        int newID = Hash(name);
        if(this.currentID == this.nextID){ //Start-up case
            if(!firstNode) {
                if (newID > this.currentID) {
                    this.nextID = newID;
                    return true;
                } else {
                    return false;
                }
            }else{
                setNextId(newID);
                return true;
            }
        }
        if(this.currentID < this.nextID){ //Normal node
            if ((this.currentID < newID) & (newID < this.nextID)) {
                setNextId(newID);
                return true;
            } else {
                return false;
            }
        }
        if(this.currentID > this.nextID){ //Edge node
            if(newID > this.nextID){
                setNextId(newID);
                    return true;
            } else{
                return false;
            }
        }else{
            System.out.println("Error in updating indexes");
            return false;
        }
    }
    public boolean updatePreviousID(String name) {
        int newID = Hash(name);
        if(this.currentID==this.previousID){
            if(!firstNode) {
                if (newID < this.currentID) {
                    this.previousID = newID;
                    return true;
                } else {
                    return false;
                }
            }else{
                setPreviousId(newID);
                return true;
            }
        }
        if(this.currentID > this.previousID){
            if ((this.previousID < newID) & (newID < this.currentID)) {
                setPreviousId(newID);
                return true;
            } else {
                return false;
            }
        }
        if(this.currentID < this.previousID){
            if(newID > this.previousID){
                setPreviousId(newID);
                return true;
            }else{
                return false;
            }
        }
        else{
            return false;
        }
    }
    public void setServerIP(String IP){this.ServerIP = IP;}
    public int getPreviousId() {return previousID;}
    public void setPreviousId(int previousId) {
        previousID = previousId;}
    public int getNextId() {return nextID;}
    public void setNextId(int nextId) {
        nextID = nextId;}
    public int getCurrentId() {return currentID;}
    public void setCurrentId(int currentId) {
        currentID = currentId;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    private int Hash(String name){
        double max = 2147483647;
        double min = -2147483647;
        return (int) ((name.hashCode()+max)*(32768/(max+abs(min))));
    }

    public void shutdown(){
        System.out.println("Client: Shutting down...");
        // Getting IP-Addresses of previous and next node
        System.out.println("Client: Shutdown: Obtaining IP-adresses");
        String ipPreviousNode = httpModule.sendIPRequest(previousID);
        String ipNextNode = httpModule.sendIPRequest(nextID);

        // Update the next and previous node parameters.
        System.out.println("Client: Shutdown: Updating previous and next node");
        httpModule.sendUpdatePreviousNode(ipPreviousNode,nextID);
        httpModule.sendUpdateNextNode(ipNextNode,previousID);

        // Get the replicated files and update the previous node
        Vector<String> replicatedFiles = new Vector<>();
        Vector<String> replicatedOwnerFiles = new Vector<>();
        String ipPreviousPreviousNode = httpModule.sendPreviousIPRequest(previousID);
        for (int i=0;fileLogList.size()<i;i++) {
            for (int j=0;fileLogList.get(i).getReplicatedOwners().size()<j;j++) {
                if (fileLogList.get(i).getReplicatedOwners().get(j) == this.IPAddres) {
                    if (fileLogList.get(i).getOwner() == previousID) {
                        JSONObject message = new JSONObject();
                        message.put("Sender","Client");
                        message.put("Message","Replication");
                        message.put("IP",this.IPAddres);
                        message.put("fileLog",fileLogList.get(i));
                        httpModule.sendReplication(message,fileLogList.get(i).getReplicatedOwners().get(j));
                    }
                    else {
                        JSONObject message = new JSONObject();
                        message.put("Sender","Client");
                        message.put("Message","Replication");
                        message.put("IP",this.IPAddres);
                        message.put("fileLog",fileLogList.get(i));
                        httpModule.sendReplication(message,ipPreviousPreviousNode);
                    }
                }
            }
        }



        // Remove the node from the naming server's map.
        System.out.println("Client: Shutdown: Notifying server");
        httpModule.sendShutdown(this.name);
        System.out.println("Client: Shutdown completed");
    }
    public void Failure(String nodeName) throws IOException, InterruptedException {
        System.out.println("Client: Failure: ");
        JSONObject message = new JSONObject();
        message.put("Failed node ID", Hash(nodeName));
        message.put("Failed node name",nodeName);
        httpModule.sendFailure(message);
    }
    public void Discovery(){
        System.out.println("Client: Discovery...");
        JSONObject message = new JSONObject();
        message.put("Type","Client");
        message.put("Message","Discovery");
        message.put("Name",this.name);
        message.put("IPAddress",this.IPAddres);
        this.httpModule.sendDiscovery(message);

    }

    public void print(){
        System.out.println(" ");
        System.out.println("Client");
        System.out.println("-------------------");
        System.out.println("Name: "+this.name);
        System.out.println("First node: "+this.firstNode);
        System.out.println("IP-Address: "+this.IPAddres);
        System.out.println("PreviousID: "+this.previousID);
        System.out.println("ID: "+this.currentID);
        System.out.println("NextID: "+this.nextID);
        System.out.println("-------------------");
    }

    public void verifyFiles(){
        File directory = new File("src/main/java/ProjectY/Client/Files");
        File[] contentOfDirectory = directory.listFiles();
        for (File object : contentOfDirectory) {
            if (object.isFile()) {
                System.out.println("Verify file name: " + object.getName());
                FileLog fileLog = new FileLog(object.getName(), Hash(object.getName()));
                fileLog.setOwner(this.currentID);
                fileLogList.add(fileLog);
                System.out.println(fileLogList);
            }
        }
        for (FileLog fileLog : fileLogList) {
            JSONObject message = new JSONObject();
            JSONArray replicatedOwners = new JSONArray();
            JSONArray downloadLocations = new JSONArray();
            replicatedOwners.addAll(fileLog.getReplicatedOwners());
            downloadLocations.addAll(fileLog.getDownloadLocations());

            message.put("Sender", "Client");
            message.put("Message", "Replication");
            message.put("fileName", fileLog.getFileName());
            message.put("fileID",fileLog.getFileID());
            message.put("owner",fileLog.getOwner());
            message.put("replicatedOwners",replicatedOwners);
            message.put("downloadLocations",downloadLocations);
            httpModule.sendReplication(message);
        }
    }

    public void replication(FileLog fileLog, String IP) {
        fileLog.addReplicatedOwner(this.IPAddres);
        this.tcpModule.sendReplicatedFiles(IP, fileLog.getFileName());
    }

    // Check the local folder for changes at regular time intervals
    public void replicationUpdate(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                File folder = new File("src/main/java/ProjectY/Client/Files");
                File[] files = folder.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            if (!getFileNamesList(fileLogList).contains(file.getName())) {
                                System.out.println("Client: New file detected: " + file.getName());
                                FileLog fileLog = new FileLog(file.getName(), Hash(file.getName()));
                                fileLog.setOwner(currentID);
                                fileLogList.add(fileLog);
                                replication(fileLog, ServerIP);
                            }
                        }
                    }
                }
            }
        }, 0, 5000);

    }

    public Vector<String> getFileNamesList(Vector<FileLog> fileLogList) {
        Vector <String> fileNamesList = new Vector<>();
        for (int i=0; i < fileLogList.size(); i++){
            fileNamesList.add(fileLogList.get(i).getFileName());
        }
        return fileNamesList;
    }
}
