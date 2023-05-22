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
        Vector<String> replicatedFiles = new Vector<>();
        Vector<String> replicatedOwnerFiles = new Vector<>();
        String ipPreviousPreviousNode = httpModule.sendPreviousIPRequest(previousID);
        for (int i=0;fileLogList.size()<i;i++) {
            for (int j=0;fileLogList.get(i).getReplicatedOwners().size()<j;j++) {
                if (fileLogList.get(i).getReplicatedOwners().get(j) == this.IPAddres) {
                    if (fileLogList.get(i).getOwner() == previousID) {
/*                        JSONObject message = new JSONObject();
                        message.put("Sender","Client");
                        message.put("Message","Replication");
                        message.put("IP",this.IPAddres);
                        message.put("fileLog",fileLogList.get(i));
                        //httpModule.sendReplication(message,fileLogList.get(i).getReplicatedOwners().get(j));*/
                        tcpModule.sendFile(fileLogList.get(i).getReplicatedOwners().get(j), fileLogList.get(i).getFileName());
                        fileLogList.get(i).updateReplicatedOwner(this.IPAddres,ipPreviousNode);
                        //fileLogList.get(i).
                    }
                    else {
/*                        JSONObject message = new JSONObject();
                        message.put("Sender","Client");
                        message.put("Message","Replication");
                        message.put("IP",this.IPAddres);
                        message.put("fileLog",fileLogList.get(i));
                        //httpModule.sendReplication(message,ipPreviousPreviousNode);*/
                        tcpModule.sendFile(ipPreviousPreviousNode, fileLogList.get(i).getFileName());
                        fileLogList.get(i).updateReplicatedOwner(this.IPAddres,ipPreviousPreviousNode);
                    }
                }
            }
            if (fileLogList.get(i).getOwnerIP() == this.IPAddres) {
                if (!fileLogList.get(i).getReplicatedOwners().isEmpty()) {
                    fileLogList.get(i).setOwner(previousID);
                    fileLogList.get(i).setOwnerIP(ipPreviousNode);
                }
                //else {
                //deleteFile(fileLogList.get(i).getFileName());
                //}
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

    // MOET NOG GEBEUREN: NIEUWE NODE -> CHECK OF REPLICATED FILES MOETEN VERPLAATST WORDEN
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
            JSONArray replicatedOwners = new JSONArray();
            JSONArray downloadLocations = new JSONArray();
            replicatedOwners.addAll(fileLog.getReplicatedOwners());
            downloadLocations.addAll(fileLog.getDownloadLocations());

            message.put("Sender", "Client");
            message.put("Message", "Replication");
            message.put("fileName", fileLog.getFileName());
            message.put("fileID",fileLog.getFileID());
            message.put("owner",fileLog.getOwner());
            message.put("ownerIP",fileLog.getOwnerIP());
            message.put("replicatedOwners",replicatedOwners);
            message.put("downloadLocations",downloadLocations);
            httpModule.sendReplication(message);
        }
    }

    public void replication(FileLog fileLog, String IP) {
        fileLog.addReplicatedOwner(this.IPAddres);
        this.tcpModule.sendFile(IP,fileLog.getFileName());
    }

    // Check the local folder for changes at regular time intervals
    public void replicationUpdate(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                File folder = new File("src/main/java/ProjectY/Client/Files/local");
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
                        if (!fileNames.contains(fileLog)) {
                            Vector <String> replicatedOwners = fileLog.getReplicatedOwners();
                            for (int i=0;i < replicatedOwners.size();i++) {
                                JSONObject message = new JSONObject();
                                message.put("Sender", "Client");
                                message.put("Message", "Replication delete file");
                                message.put("fileName", fileLog.getFileName());
                                httpModule.sendDeleteFile(message, replicatedOwners.get(i));
                            }
                            fileLogList.remove(fileLog);
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
