package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.abs;

public class Client {
    private int previousID;
    private int nextID;
    private int currentID;
    private String name;
    private String IPAddres;
    public String NodeType = "FirstNode";
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
        verifyFiles();
    }

    public boolean updateNextID(String name){
        int newID = Hash(name);
        if(NodeType=="FirstNode"){
            this.previousID=newID;
            this.nextID=newID;
            return true;
        }else if (NodeType=="EdgeNodeRight"){
            if(newID>this.currentID || newID<nextID){
                this.nextID=newID;
                return true;
            }else{
                return false;
            }
        }else{
            if(newID>this.currentID && newID<this.nextID){
                this.nextID=newID;
                return true;
            }else{
                return false;
            }
        }
    }
    public boolean updatePreviousID(String name) {
        int newID = Hash(name);
        if(NodeType=="FirstNode"){
            this.previousID=newID;
            this.nextID=newID;
            return true;
        }else if(NodeType=="EdgeNodeLeft"){
            if(newID > this.previousID || newID < currentID){
                this.previousID=newID;
                return true;
            }else{
                return false;
            }
        }else{
            if(newID < currentID && newID > previousID) {
                this.previousID = newID;
                return true;
            }else{
                return false;
            }

        }
    }
    public int getPreviousId() {return previousID;}
    public void setPreviousId(int previousId) {previousID = previousId;}
    public int getNextId() {return nextID;}
    public void setNextId(int nextId) {nextID = nextId;}
    public int getCurrentId() {return currentID;}
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
        String ipPreviousPreviousNode = httpModule.sendPreviousIPRequest(previousID);
        for (FileLog fileLog : fileLogList) {
            if (fileLog.getReplicatedOwner() == this.IPAddres) {
                if (fileLog.getOwner() == previousID) {
                    tcpModule.sendFile(fileLog.getOwnerIP(), ipPreviousPreviousNode, fileLog.getFileName());
                    fileLog.setReplicatedOwner(ipPreviousPreviousNode);
                }
                else {
                    tcpModule.sendFile(fileLog.getOwnerIP(), ipPreviousNode, fileLog.getFileName());
                    fileLog.setReplicatedOwner(ipPreviousNode);
                }
            }
            if (fileLog.getOwnerIP() == this.IPAddres) {
                if (fileLog.getReplicatedOwner().isEmpty()) {
                    deleteFile(fileLog.getFileName());
                    fileLogList.remove(fileLog);
                }
                else {
                    httpModule.sendDeleteFile(fileLog.getReplicatedOwner(), fileLog.getFileName());
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
        //this.httpModule.askReplicationFiles(httpModule.sendIPRequest(previousID));
    }

    public String getName() {return name;}

    public void askReplicationFiles(String newNode, String newNodeIP) {
        for(FileLog fileLog : fileLogList){
           if(currentID < Hash(newNode) && Hash(newNode) < fileLog.getFileID()){
               tcpModule.sendFile(fileLog.getOwnerIP(), newNodeIP, fileLog.getFileName());
               deleteFile(fileLog.getFileName());
           }
        }
    }
    public void print(){
        System.out.println(" ");
        System.out.println("Client");
        System.out.println("-------------------");
        System.out.println("Name: "+this.name);
        System.out.println("First node: "+this.NodeType);
        System.out.println("IP-Address: "+this.IPAddres);
        System.out.println("PreviousID: "+this.previousID);
        System.out.println("ID: "+this.currentID);
        System.out.println("NextID: "+this.nextID);
        System.out.println("-------------------");
    }

    public void verifyFiles(){
        File directory = new File("src/main/java/ProjectY/Client/Files/local");
        File[] contentOfDirectory = directory.listFiles();
        for (File object : contentOfDirectory) {
            if (object.isFile()) {
                System.out.println("Verify file name: " + object.getName());
                FileLog fileLog = new FileLog(object.getName(), Hash(object.getName()));
                fileLog.setOwner(this.currentID);
                fileLog.setOwnerIP(this.IPAddres);
                fileLogList.add(fileLog);
                System.out.println(fileLogList);
            }
        }
        for (FileLog fileLog : fileLogList) {
            JSONObject message = new JSONObject();

            message.put("Sender", "Client");
            message.put("Message", "Replication");
            message.put("fileID",fileLog.getFileID());
            JSONObject response = httpModule.sendReplication(message);
            replication(fileLog, (String) response.get("ReplicatedOwnerIP"));
        }

        this.httpModule.askReplicationFiles(httpModule.sendIPRequest(previousID));
    }

    public void replication(FileLog fileLog, String IP) {
        fileLog.setReplicatedOwner(this.IPAddres);
        this.tcpModule.sendFile(fileLog.getOwnerIP(), IP,fileLog.getFileName());
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
                    Vector<String> fileNames = new Vector<>();
                    for (File file : files) {
                        if (file.isFile()) {
                            fileNames.add(file.getName());
                            if (!getFileNamesList(fileLogList).contains(file.getName())) {
                                System.out.println("Client: New file detected: " + file.getName());
                                FileLog fileLog = new FileLog(file.getName(), Hash(file.getName()));
                                fileLog.setOwner(currentID);
                                fileLog.setOwnerIP(IPAddres);
                                fileLogList.add(fileLog);
                                replication(fileLog, ServerIP);
                            }
                        }
                    }
                    for (FileLog fileLog : fileLogList) {
                        if (!fileNames.contains(fileLog.getFileName())) {
                            if(fileLog.getOwnerIP() == IPAddres) {
                                httpModule.sendDeleteFile(fileLog.getReplicatedOwner(), fileLog.getFileName());
                                fileLogList.remove(fileLog);
                            }
                            else {
                                tcpModule.sendFile(fileLog.getOwnerIP(), fileLog.getReplicatedOwner(), fileLog.getFileName());
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

    public void run(){
        boolean running=true;
        while(running){
            System.out.println("Enter command: ");
            String command = System.console().readLine();
            if(command.equals("Shutdown")){
                shutdown();
            }
            if(command.equals("Discovery")){
                Discovery();
            }
            if(command.equals("Kill")){
                running=false;
            }
            if(command.equals("Files")){
                verifyFiles();
            }
        }
    }

    public void updateNodeType(){
        if(nextID<currentID){
            NodeType="EdgeNodeRight";
        }
        if(previousID>currentID){
            NodeType="EdgeNodeLeft";
        }
        if(nextID==currentID&&previousID==currentID){
            NodeType="FirstNode";
        }
        if(previousID<currentID && currentID<nextID){
            NodeType="NormalNode";
        }
        print();
    }
    public void deleteFile(String fileName){
        String path = "src/main/java/ProjectY/Client/Files/"+fileName;
        File file = new File(path);

        if (file.exists()) {
            try {
                if (file.delete()) {
                    System.out.println("Client: File deleted successfully.");
                } else {
                    System.out.println("Client: Failed to delete the file.");
                }
            } catch (SecurityException e) {
                System.out.println("Client: Permission denied. Unable to delete the file.");
            }
        } else {
            System.out.println("Client: File does not exist.");
        }
        fileLogList.remove(fileName);
    }


    public String getIPAddres() {
        return IPAddres;
    }
}
