package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Vector;

import static java.lang.Math.abs;

public class Client {
    private int previousID;
    private int nextID;
    private int currentID;
    private String name;
    private String IPAddres;
    private HttpModule httpModule = new HttpModule(this);
    private String ServerIP = "192.168.1.1";
    private Vector<FileLog> fileLogList = new Vector<>();

    public Client() {
        this.currentID = Hash("Test");
        this.previousID = this.currentID;
        this.nextID = this.currentID;
        System.out.println("Enter name: ");
        this.name = "test";
        System.out.println("Enter IP-Address: ");
        this.IPAddres = "192.168.1.2";
        //Discovery();
        verifyFiles();
    }

    public boolean updateNextID(String name){
        int newID = Hash(name);
        if(this.currentID ==this.nextID){
         if(newID>this.currentID){
             this.nextID = newID;
             return true;
         }else{
             return false;
         }
        }else {
            if ((this.currentID < newID) & (newID < this.nextID)) {
                setNextId(newID);
                return true;
            } else {
                return false;
            }
        }
    }
    public boolean updatePreviousID(String name) {
        int newID = Hash(name);
        if(this.currentID==this.previousID){
            if(newID < this.currentID){
                this.previousID = newID;
                return true;
            }else{
                return false;
            }
        }else {
            if ((this.previousID < newID) & (newID < this.currentID)) {
                setPreviousId(newID);
                return true;
            } else {
                return false;
            }
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

    public void shutdown() throws IOException, InterruptedException {
        HttpClient httpclient = HttpClient.newHttpClient();

        // Get the IP and ID of the previous and next node.
        HttpRequest requestPreviousIPAddress = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ProjectY/NamingServer/getIPAddress/"+ getPreviousId()))
                .build();
        HttpResponse<String> responsePreviousIPAddress =
                httpclient.send(requestPreviousIPAddress, HttpResponse.BodyHandlers.ofString());
        // Test
        System.out.println(responsePreviousIPAddress.body());

        HttpRequest requestNextIPAddress = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ProjectY/NamingServer/getIPAddress/"+ getNextId()))
                .build();
        HttpResponse<String> responseNextIPAddress =
                httpclient.send(requestNextIPAddress, HttpResponse.BodyHandlers.ofString());
        // Test
        System.out.println(responseNextIPAddress.body());

        // Update the next and previous node parameters.
        HttpRequest requestPreviousNode = HttpRequest.newBuilder()
                .uri(URI.create(responsePreviousIPAddress.body()+":8080/ProjectY/Update/PreviousNode/"+ getNextId()))
                .build();
        HttpResponse<String> responsePreviousNode =
                httpclient.send(requestPreviousNode, HttpResponse.BodyHandlers.ofString());

        HttpRequest requestNextNode = HttpRequest.newBuilder()
                .uri(URI.create(responseNextIPAddress.body()+":8080/ProjectY/Update/NextNode/"+ getPreviousId()))
                .build();
        HttpResponse<String> responseNextNode =
                httpclient.send(requestNextNode, HttpResponse.BodyHandlers.ofString());

        // Remove the node from the naming server's map.
        HttpRequest requestDeleteNode = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ProjectY/NamingServer/deleteNode"+this.name))
                .build();
        HttpResponse<String> responseDeleteNode =
                httpclient.send(requestDeleteNode, HttpResponse.BodyHandlers.ofString());

    }
    public void failure(String nodeName) throws IOException, InterruptedException {
        HttpClient httpclient = HttpClient.newHttpClient();

        // Get the IP and ID of the previous and next nodes.
        HttpRequest requestFailure = HttpRequest.newBuilder()
                .uri(URI.create("localhost:8080/ProjectY/NamingServer/failure/"+nodeName))
                .build();
        HttpResponse<String> response =
                httpclient.send(requestFailure, HttpResponse.BodyHandlers.ofString());

        JSONObject message = new ObjectMapper().readValue(response.body(), JSONObject.class);

        // Send the ID of the next node to the previous node.
        HttpRequest requestPreviousNode = HttpRequest.newBuilder()
                .uri(URI.create(message.get("previousIP")+":8080/ProjectY/Update/PreviousNode/"+message.get("nextId")))
                .build();
        HttpResponse<String> responsePreviousNode =
                httpclient.send(requestPreviousNode, HttpResponse.BodyHandlers.ofString());

        // Send the ID of the previous node to the next node.
        HttpRequest requestNextNode = HttpRequest.newBuilder()
                .uri(URI.create(message.get("nextIP")+":8080/ProjectY/Update/NextNode/"+message.get("previousId")))
                .build();
        HttpResponse<String> responseNextNode =
                httpclient.send(requestNextNode, HttpResponse.BodyHandlers.ofString());
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
        System.out.println("Client");
        System.out.println("-------------------");
        System.out.println("Name: "+this.name);
        System.out.println("IP-Address: "+this.IPAddres);
        System.out.println("ID: "+this.currentID);
        System.out.println("NextID: "+this.nextID);
        System.out.println("PreviousID: "+this.previousID);
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
            }
        }
        JSONObject message = new JSONObject();
        message.put("Sender", "Client");
        message.put("Message", "Replication");
        message.put("FileLogList", fileLogList);
        httpModule.sendReplication(message);
    }

    public void replication(FileLog fileLog) {
        fileLog.addReplicatedOwner(this.IPAddres);
    }
}
