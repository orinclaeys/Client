package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import org.json.simple.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static java.lang.Math.abs;

public class Client {
    private int previousID;
    private int nextID;
    private final int currentID;
    private String name;
    private String IPAddres;
    public String NodeType = "FirstNode";
    private final HttpModule httpModule = new HttpModule();
    private final TcpModule tcpModule = new TcpModule();
    private final Vector<FileLog> fileLogList = new Vector<>();
    private Timer timer;
    public SyncAgent syncAgent = new SyncAgent();
    private Map<String, Boolean> syncList = new HashMap<>();
    private final Boolean isLocked = false;
    private final Thread syncAgentThread = new Thread(syncAgent);

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
        //replicationUpdate();
        // Start syncAgentThread
        //Thread syncAgentThread = new Thread(syncAgent);
        //syncAgentThread.start();
        print();
    }

    /**
     * Updates the next id
     * If currentID < hash < nextID, nexID = hash,
     * current node updates its own parameter nextID.
     *
     * @param name the name of the node
     * @return true if it's updated, false otherwise
     */
    public boolean updateNextID(String name){
        int newID = Hash(name);
        if(NodeType.equals("FirstNode")){
            this.previousID=newID;
            this.nextID=newID;
            return true;
        }else if (NodeType.equals("EdgeNodeRight")){
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

    /**
     * Updates the previous id
     * If previousID < hash < currentID, previousID = hash,
     * current node updates its own parameter previousID.
     *
     * @param name the name of the node
     * @return true if it's updated, false otherwise
     */
    public boolean updatePreviousID(String name) {
        int newID = Hash(name);
        if(Objects.equals(NodeType, "FirstNode")){
            this.previousID=newID;
            this.nextID=newID;
            return true;
        }else if(Objects.equals(NodeType, "EdgeNodeLeft")){
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
    public void setPreviousId(int previousId) {previousID = previousId;}
    public int getNextId() {return nextID;}
    public void setNextId(int nextId) {nextID = nextId;}
    public int getCurrentId() {return currentID;}
    /**
     * Calculates the hash based on the node name
     *
     * @param name the name of the node
     * @return the calculated hash
     */
    public int Hash(String name){
        double max = 2147483647;
        double min = -2147483647;
        return (int) ((name.hashCode()+max)*(32768/(max+abs(min))));
    }

    /**
     * Handles a shutdown: A node leaves the ring network of system Y,
     * and updates parameters of neighbour nodes and the Naming server
     */
    public void shutdown(){
        // Get the IP-addresses of previous and next node
        String ipPreviousNode = httpModule.sendIPRequest(previousID);
        String ipNextNode = httpModule.sendIPRequest(nextID);

        // Update the next and previous node parameters
        // Send the ID of the next node to the previous node.
        // In the previous node, the next node parameter will be updated according to this information.
        httpModule.sendUpdatePreviousNode(ipPreviousNode,nextID);
        // Send the ID of the previous node to the next node.
        // In the next node, the previous node parameter will be updated according to this information
        httpModule.sendUpdateNextNode(ipNextNode,previousID);

        //Cancel the timer that checks files
        timer.cancel();

        // Get the replicated files and update the previous node
        String ipPreviousPreviousNode = httpModule.sendPreviousIPRequest(previousID);
        //System.out.println("Previous previous IP = "+ipPreviousPreviousNode);
        Vector<String> deleteFiles = new Vector<>();
        Vector<String> deleteLog = new Vector<>();
        for (FileLog fileLog : fileLogList) {
            // Check if owner of the file
            if (!fileLog.getOwnerIP().equals(this.IPAddres)) {
                // Check if replicated owner of the file
                if (fileLog.getReplicatedOwner().equals(this.IPAddres)) { //Replicated Files need to be send to previousnode
                    // Check if previous owner of the file
                    if (fileLog.getOwner() == previousID) {
                        // ???
                        if (ipPreviousPreviousNode.equals(IPAddres)) {
                            deleteFiles.add(fileLog.getFileName());
                            httpModule.resetFileInformation(fileLog.getOwnerIP(), fileLog.getFileName());
                        }
                        // Send to previous previous node
                        else{
                            tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), ipPreviousPreviousNode, fileLog.getFileName());
                            httpModule.sendFileInformationUpdate(fileLog.getOwnerIP(), fileLog.getFileName(), ipPreviousPreviousNode);
                            deleteFiles.add(fileLog.getFileName());
                        }
                    }
                    // Send to previous node
                    else {
                        tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), ipPreviousNode, fileLog.getFileName());
                        httpModule.sendFileInformationUpdate(fileLog.getOwnerIP(), fileLog.getFileName(), ipPreviousNode);
                        deleteFiles.add(fileLog.getFileName());
                    }
                }
            }
            //
            else {  //Local files need to be deleted by replicator and taken out of fileList?
                // Check if not replicated
                if (fileLog.getReplicatedOwner() != null) {
                    httpModule.sendDeleteFile(fileLog.getReplicatedOwner(), fileLog.getFileName());
                    deleteLog.add(fileLog.getFileName());
                }else{
                    deleteLog.add(fileLog.getFileName());
                }
            }
        }
        for(int i=0;i<deleteFiles.size();i++){
            deleteFile(deleteFiles.get(i));
        }
        for(int i=0;i<deleteLog.size();i++){
            for(int j=0;j<fileLogList.size();j++){
                if(fileLogList.get(j).getFileName().equals(deleteLog.get(i))){
                    fileLogList.remove(j);
                }
            }
        }
        // Remove the node from the naming server's map
        httpModule.sendShutdown(this.name);
    }

    /**
     * Failure: This algorithm is activated in every exception thrown during communication
     * with other nodes. This allows distributed detection of node failure
     * Request the previous node and next node parameters from the nameserver
     *
     * @param nodeID the ID of the node
     */

    public void Failure(int nodeID) throws IOException, InterruptedException {
        System.out.println("Client: Failure: ");
        //Notifying server of the failure
        httpModule.sendFailure(nodeID);

        //Create and run the failureAgent
        FailureAgent failureAgent = new FailureAgent(currentID, nodeID);
        Thread failureAgentThread = new Thread(failureAgent);
        failureAgentThread.start();

        //Sending the agent to the next node
        String nextIP = httpModule.sendIPRequest(nextID);
        JSONObject message = new JSONObject();
        message.put("CurrentID",failureAgent.getCurrentID());
        message.put("FailingID",failureAgent.getFailingID());
        httpModule.sendFailureAgent(nextIP,message);
        // Request the previous node and next node parameters from the nameserver


    }
    public void startFailureAgent(int currentID, int failingID){
        if(this.currentID!=currentID) {
            //Create and run the failureAgent
            FailureAgent failureAgent = new FailureAgent(currentID, failingID);
            Thread failureAgentThread = new Thread(failureAgent);
            failureAgentThread.start();

            //Sending the agent to the next node
            String nextIP = httpModule.sendIPRequest(nextID);
            JSONObject message = new JSONObject();
            message.put("CurrentID", failureAgent.getCurrentID());
            message.put("FailingID", failureAgent.getFailingID());
            httpModule.sendFailureAgent(nextIP, message);
        }
    }

    /**
     * Handles discovery: Auto-discover the Naming server and existing nodes in the network
     * Starting each node, initializing local parameters (previous, nextnode), and updating
     * parameters of existing nodes
     * During bootstrap the node will send its name and its IP address
     * to all nodes and the Naming server in the network using multicast
     */
    public void Discovery(){
        //System.out.println("Client: Discovery...");
        JSONObject message = new JSONObject();
        message.put("Type","Client");
        message.put("Message","Discovery");
        message.put("Name",this.name);
        message.put("IPAddress",this.IPAddres);
        this.httpModule.sendDiscovery(message);
    }

    public void askReplicationFiles(String newNode, String newNodeIP) {
        //System.out.println(fileLogList);
        Vector<String> deletedFiles = new Vector<>();
        for(FileLog fileLog : fileLogList){
            //System.out.println(fileLog.getFileName());
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
            //System.out.println("Deleting "+fileName);
        }
    }
    public void print(){
        System.out.println(" ");
        System.out.println("Client");
        System.out.println("-------------------");
        System.out.println("Name: "+this.name);
        System.out.println("Node type: "+this.NodeType);
        System.out.println("IP-Address: "+this.IPAddres);
        System.out.println("PreviousID: "+this.previousID);
        System.out.println("ID: "+this.currentID);
        System.out.println("NextID: "+this.nextID);
        System.out.println("FileLogList: "+this.fileLogList);
        System.out.println("-------------------");
    }

    /**
     * Replication: All files that are stored on each node should be replicated to corresponding nodes in system Y.
     * This way, a new node to which the file is replicated becomes the owner of the file.
     * For all files, hash values are calculated and
     * the local node has to report that to the naming server
     */
    public void verifyFiles(){
        File directory = new File("src/main/java/ProjectY/Client/Files");
        File[] contentOfDirectory = directory.listFiles();
        for (File object : contentOfDirectory) {  //Put files in fileLogList
            if (object.isFile()) {
                //System.out.println("Verify file name: " + object.getName());
                FileLog fileLog = new FileLog(object.getName(), Hash(object.getName()));
                fileLog.setOwner(this.currentID);
                fileLog.setOwnerIP(this.IPAddres);
                fileLogList.add(fileLog);
                //System.out.println(fileLogList);
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
        if(!Objects.equals(this.NodeType, "FirstNode")) {         //ask from previousnode which files to replicate
            this.httpModule.askReplicationFiles(httpModule.sendIPRequest(previousID), name, IPAddres);
        }
    }

    public void replication(FileLog fileLog, String IP) {
        if(IP!=null) {
            fileLog.setReplicatedOwner(IP);
            this.tcpModule.sendFile(fileLog.getOwner(),fileLog.getOwnerIP(), IP, fileLog.getFileName());
        }
    }
    /**
     * Replication: If new files are added locally to certain node, or deleted from a node,
     * this state has to be synchronized in the whole system.
     * If a new file is added, then it has to be replicated.
     * Otherwise, if deleted, it has to be deleted from the replicated files of the file owner as well.
     */
    public void replicationUpdate(){
        // In order to detect new files being added to the node,
        // a thread can be executed that checks for changes on the local folder at regular intervals.
        timer=new Timer();
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
                            // Check if file added
                            if (!getFileNamesList(fileLogList).contains(file.getName())) {
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
                                }else{
                                    replicatedOwnerIP = httpModule.sendIPRequest(previousID);
                                    replication(fileLog,replicatedOwnerIP);
                                }
                            }
                        }
                    }
                    for (int i=0;i< fileLogList.size();i++) {
                        // Check if file deleted
                        if (!fileNames.contains(fileLogList.get(i).getFileName())) {
                            // Check if owner
                            if(fileLogList.get(i).getOwnerIP().equals(IPAddres)) {
                                // Delete the file everywhere
                                httpModule.sendDeleteFile(fileLogList.get(i).getReplicatedOwner(), fileLogList.get(i).getFileName());
                                fileLogList.remove(fileLogList.get(i));
                            }
                            else {
                                // Request the file (replicated owner)
                                httpModule.getFile(fileLogList.get(i).getOwnerIP(),fileLogList.get(i).getFileName());
                                break;
                            }
                        }
                    }
                }
            }
        }, 0, 5000);
    }

    public FileLog getFileLog(String fileName){
        FileLog response = null;
        for(FileLog fileLog: fileLogList){
            if(fileLog.getFileName().equals(fileName)) {
                response = fileLog;
            }
        }
        return response;
    }

    public Vector<String> getFileNamesList(Vector<FileLog> fileLogList) {
        Vector <String> fileNamesList = new Vector<>();
        for (FileLog fileLog : fileLogList) {
            fileNamesList.add(fileLog.getFileName());
        }
        return fileNamesList;
    }


    public Vector<String> getReplicatedOwnerFileNamesList() {
        Vector<String> replicatedOwnerFileNamesList = new Vector<>();
        for (FileLog fileLog : fileLogList){
            if(Objects.equals(fileLog.getReplicatedOwner(), IPAddres)) {
                replicatedOwnerFileNamesList.add(fileLog.getFileName());
            }
        }
        return replicatedOwnerFileNamesList;
    }

    public void run(){
        boolean running=true;
        while(running){
            System.out.println("Enter command: ");
            String command = System.console().readLine();
            if(command.equals("Print")){
                print();
            }
            if(command.equals("Shutdown")){
                shutdown();
                syncAgentThread.interrupt();
            }
            if(command.equals("Discovery")){
                Discovery();
            }
            if(command.equals("Kill")){
                running=false;
            }
            if(command.equals("Files")){
                verifyFiles();
                print();
                replicationUpdate();
            }
            if(command.equals("List")){
                System.out.println(fileLogList);
            }
            if(command.equals("Agents")){
                syncAgentThread.start();
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
        //System.out.println("File "+fileName+" deleted.");
    }
    public void addReplicatedFile(String fileName, String ownerIP, int ownerID){
        boolean present=false;
        for(FileLog fileLog: fileLogList){
            if (fileLog.getFileName().equals(fileName)) {
                present = true;
                break;
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
                if(ReplicationIP!=null) {
                    tcpModule.sendFile(fileLog.getOwner(), fileLog.getOwnerIP(), fileLog.getReplicatedOwner(), fileLog.getFileName());
                }
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
    public Map<String, Boolean> getOwnerList() {
        Map<String, Boolean> ownerList = new HashMap<>();
        for (FileLog fileLog : fileLogList) {
            if (fileLog.getOwner() == currentID)
                // Information on whether there is an active lock on the node or not
                // true = locked
                // false = unlocked
                ownerList.put(fileLog.getFileName(), isLocked);
        }
        return ownerList;
    }

    public Map<String, Boolean> getSyncList() {return syncList;}

    public void setSyncList(Map<String, Boolean> syncList) {this.syncList = syncList;}

    // Failure agent
    public Vector<String> getFailureFileNameList(int failureID) {
        Vector <String> failureFileNameList = new Vector<>();
        for (FileLog fileLog : fileLogList) {
            if (fileLog.getOwner() == failureID) {
                failureFileNameList.add(fileLog.getFileName());
            }
        }
        return failureFileNameList;
    }

    public boolean hasFile(String fileName) {
        boolean response = false;
        File folder = new File("src/main/java/ProjectY/Client/Files");
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().equals(fileName)) {
                        response = true;
                    }
                }
            }
        }
        return response;
    }

    public void setNewOwner(String fileName) {
        for (FileLog fileLog : fileLogList) {
            if (Objects.equals(fileLog.getFileName(), fileName)) {
                fileLog.setOwner(currentID);
                fileLog.setOwnerIP(IPAddres);
            }
        }
    }
}
