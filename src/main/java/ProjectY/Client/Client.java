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
    public SyncAgent syncAgent = new SyncAgent();


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
    }

    public void initialize(){
        Discovery();
        //verifyFiles();
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
            if(!fileLog.getOwnerIP().equals(this.IPAddres)) { //File is replicated
                if (fileLog.getReplicatedOwner().equals(this.IPAddres)) { //Replicated Files need to be send to previousnode
                    if (fileLog.getOwner() == previousID) { //owner==previousnode ==> previous previous node
                        if (NodeType.equals("NormalNode")) { //send to previous previous node
                            tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), ipPreviousPreviousNode, fileLog.getFileName());
                            httpModule.sendFileInformationUpdate(fileLog.getOwnerIP(), fileLog.getFileName(), ipPreviousPreviousNode);
                            deleteFile(fileLog.getFileName());
                        }
                        if (NodeType.equals("EdgeNodeRight") || NodeType.equals("EdgeNodeLeft")) { //send back to owner
                            deleteFile(fileLog.getFileName());
                            httpModule.sendFileInformationUpdate(fileLog.getOwnerIP(), fileLog.getFileName(), null);
                        }
                    } else { //owner!=previousnode => send to previous node
                        tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), ipPreviousNode, fileLog.getFileName());
                        httpModule.sendFileInformationUpdate(fileLog.getOwnerIP(), fileLog.getFileName(), ipPreviousNode);
                        deleteFile(fileLog.getFileName());

                    }
                }
            }
            else{  //Local files need to be deleted by replicator?
                if (fileLog.getReplicatedOwner()!=null) {  //File is not replicated
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

    }

    public String getName() {return name;}

    public void askReplicationFiles(String newNode, String newNodeIP) {
        System.out.println(fileLogList);
        Vector<String> deletedFiles = new Vector<>();
        for(FileLog fileLog : fileLogList){
            System.out.println(fileLog.getFileName());
            if(fileLog.getOwnerIP().equals(IPAddres)) {
                if(Hash(newNode)<fileLog.getFileID()) {
                    httpModule.sendDeleteFile(fileLog.getReplicatedOwner(), fileLog.getFileName());
                    fileLog.setReplicatedOwner(newNodeIP);
                    tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), newNodeIP, fileLog.getFileName());

                }
                if(fileLog.getReplicatedOwner()==null){
                    fileLog.setReplicatedOwner(newNodeIP);
                    tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), newNodeIP, fileLog.getFileName());
                }
            }else{
                if (currentID < Hash(newNode) && Hash(newNode) < fileLog.getFileID()) {
                    httpModule.sendFileInformationUpdate(fileLog.getOwnerIP(), fileLog.getFileName(), newNodeIP);
                    deletedFiles.add(fileLog.getFileName());
                }
            }

        }
        for(String fileName: deletedFiles){
            deleteFile(fileName);
            System.out.println("Deleting "+fileName);
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
        File directory = new File("src/main/java/ProjectY/Client/Files");
        File[] contentOfDirectory = directory.listFiles();
        for (File object : contentOfDirectory) {  //Put files in fileLogList
            if (object.isFile()) {
                System.out.println("Verify file name: " + object.getName());
                FileLog fileLog = new FileLog(object.getName(), Hash(object.getName()));
                fileLog.setOwner(this.currentID);
                fileLog.setOwnerIP(this.IPAddres);
                fileLogList.add(fileLog);
                System.out.println(fileLogList);
            }
        }
        for (FileLog fileLog : fileLogList) {     //send filesID's to server
            JSONObject message = new JSONObject();

            message.put("Sender", "Client");
            message.put("Message", "Replication");
            message.put("fileID",fileLog.getFileID());
            JSONObject response = httpModule.sendReplication(message);
            String replicatedOwnerIP = (String) response.get("ReplicatedOwnerIP");
            if(!Objects.equals(replicatedOwnerIP, IPAddres)) {
                replication(fileLog, replicatedOwnerIP);
            }
        }
        if(this.NodeType!="FirstNode") {         //ask from previousnode which files to replicate
            this.httpModule.askReplicationFiles(httpModule.sendIPRequest(previousID), name, IPAddres);
        }
    }

    public void replication(FileLog fileLog, String IP) {
        if(IP!=null) {
            fileLog.setReplicatedOwner(IP);
            this.tcpModule.sendFile(fileLog.getOwner(),fileLog.getOwnerIP(), IP, fileLog.getFileName());
        }
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
                                JSONObject message = new JSONObject();
                                message.put("Sender", "Client");
                                message.put("Message", "Replication");
                                message.put("fileID",fileLog.getFileID());
                                JSONObject response = httpModule.sendReplication(message);
                                String replicatedOwnerIP = (String) response.get("ReplicatedOwnerIP");
                                if(!Objects.equals(replicatedOwnerIP, IPAddres)) {
                                    replication(fileLog, replicatedOwnerIP);
                                }
                            }
                        }
                    }
                    for (int i=0;i< fileLogList.size();i++) {
                        if (!fileNames.contains(fileLogList.get(i).getFileName())) {
                            if(fileLogList.get(i).getOwnerIP().equals(IPAddres)) {
                                httpModule.sendDeleteFile(fileLogList.get(i).getReplicatedOwner(), fileLogList.get(i).getFileName());
                                fileLogList.remove(fileLogList.get(i));
                            }
                            else {
                                httpModule.getFile(fileLogList.get(i).getOwnerIP(),fileLogList.get(i).getFileName());
                                break;
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
                replicationUpdate();
            }
            if(command.equals("List")){
                System.out.println(fileLogList);
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
        for(int i=0;i<fileLogList.size();i++){
            if(fileName.equals(fileLogList.get(i).getFileName())){
                fileLogList.remove(fileLogList.get(i));
            }
        }
        System.out.println("File "+fileName+" deleted.");
    }
    public void addReplicatedFile(String fileName, String ownerIP, int ownerID){
        boolean present=false;
        for(FileLog fileLog: fileLogList){
            if(fileLog.getFileName().equals(fileName)){
                present=true;
            }
        }
        if(!present) {
            FileLog fileLog = new FileLog(fileName, Hash(fileName));
            fileLog.setOwnerIP(ownerIP);
            fileLog.setOwner(ownerID);
            fileLog.setReplicatedOwner(IPAddres);
            fileLogList.add(fileLog);
        }
    }
    public void updateReplicatedIP(String fileName,String ReplicationIP){
        for(FileLog fileLog: fileLogList){
            if(fileName.equals(fileLog.getFileName())){
                fileLog.setReplicatedOwner(ReplicationIP);
                tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), fileLog.getReplicatedOwner(),fileLog.getFileName());
            }
        }
    }
    public void getFile(String fileName){
        for(FileLog fileLog: fileLogList){
            if(fileLog.getFileName().equals(fileName)){
                tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(),fileLog.getReplicatedOwner(),fileLog.getFileName());
            }
        }
    }


    public String getIPAddres() {return IPAddres;}

    public Vector<String> getOwnerFileNamesList(Vector<FileLog> fileLogList) {
        Vector <String> fileNamesList = new Vector<>();
        for (int i=0; i < fileLogList.size(); i++){
            if (fileLogList.get(i).getOwner() == currentID)
                fileNamesList.add(fileLogList.get(i).getFileName());
        }
        return fileNamesList;
    }
}
