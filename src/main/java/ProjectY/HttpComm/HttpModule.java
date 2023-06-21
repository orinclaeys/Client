package ProjectY.HttpComm;

import ProjectY.Client.Client;
import ProjectY.Client.ClientApplication;
import ProjectY.Client.ClientService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class HttpModule{
    public HttpModule() {}
    private final Client node = ClientApplication.client;
    private final static String serverIP="172.30.0.5";

    /**
     * ---------
     * DISCOVERY
     * ---------
     */
    public void sendDiscovery(JSONObject message){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    //.uri(URI.create("http://127.0.0.1:8080/ProjectY/Discovery"))
                    .uri(URI.create("http://"+serverIP+":8080/ProjectY/Discovery"))
                    .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                    .header("Content-type", "application/json")
                    .timeout(Duration.ofSeconds(1000))
                    .build();

            HttpResponse<String> Stringresponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JSONObject response = mapper.readValue(Stringresponse.body(),JSONObject.class);

            ClientService service = new ClientService();
            service.handleDiscoveryResponse(response);
            Vector<String> IPlist = new Vector<String>((ArrayList<String>) response.get("IPlist"));
            for (String ipAddr : IPlist) {
                HttpRequest request2 = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + ipAddr + ":8081/ProjectY/Discovery"))
                        .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(1000))
                        .build();

                HttpResponse<String> Stringresponse2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
                JSONObject response2 = mapper.readValue(Stringresponse2.body(), JSONObject.class);
                service.handleDiscoveryResponse(response2);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public String sendIPRequest(int ID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create("http://localhost:8080/ProjectY/NamingServer/getIPAddress/"+ ID))
                .uri(URI.create("http://"+serverIP+":8080/ProjectY/NamingServer/getIPAddress/"+ ID))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public String sendPreviousIPRequest(int previousID) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+serverIP+":8080/ProjectY/NamingServer/getPreviousIPAddress/"+ previousID))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendUpdateNextNode(String IPAddress, int previousID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest requestPreviousNode = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IPAddress+":8081/ProjectY/Update/NextNode/"+ previousID))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            httpClient.send(requestPreviousNode, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendUpdatePreviousNode(String IPAddress, int nextID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest requestPreviousNode = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IPAddress+":8081/ProjectY/Update/PreviousNode/"+ nextID))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            httpClient.send(requestPreviousNode, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * -----------
     * REPLICATION
     * -----------
     */
    public void askReplicationFiles(String DestinationIP, String nodeName, String nodeIP){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+DestinationIP+":8081/ProjectY/Client/Discovery/askReplicationFiles/"+nodeName+"/"+nodeIP))
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void getFile(String IP, String fileName){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IP+":8081/ProjectY/Client/replication/sendFile/"+fileName))
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void resetFileInformation(String IP, String fileName){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IP+":8081/ProjectY/Client/replication/resetFile/"+fileName))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendDeleteFile(String ip, String fileName){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+ip+":8081/ProjectY/Client/replication/sendDeleteFile/"+fileName))
                .DELETE()
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendFailure(int nodeID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+serverIP+":8080/ProjectY/Failure/"+nodeID))
                .DELETE()
                .build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendFileInformation(String IP, JSONObject message){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IP+":8081/ProjectY/Client/replication/sendFileInformation"))
                .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                .header("Content-type", "application/json")
                .timeout(Duration.ofSeconds(1000))
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendFileInformationUpdate(String IP, String fileName, String ReplicatedIP){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IP+":8081/ProjectY/Client/replication/update/"+fileName+"/"+ReplicatedIP))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public JSONObject sendReplication(JSONObject message){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                //.uri(URI.create("http://"+Client.ServerIP+":8080/ProjectY/NamingServer/replication"))
                .uri(URI.create("http://"+serverIP+":8080/ProjectY/NamingServer/replication"))
                .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                .header("Content-type", "application/json")
                .timeout(Duration.ofSeconds(1000))
                .build();
        try {
            HttpResponse<String> stringResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JSONObject response =  mapper.readValue(stringResponse.body(), JSONObject.class);
            return response;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendShutdown(String name){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest requestDeleteNode = HttpRequest.newBuilder()
                //.uri(URI.create("http://localhost:8080/ProjectY/NamingServer/deleteNode"+name))
                .uri(URI.create("http://"+serverIP+":8080/ProjectY/NamingServer/deleteNode/"+name))
                .DELETE()
                .build();
        try {
            httpClient.send(requestDeleteNode, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ----------
     * SYNC AGENT
     * ----------
     */
    public Map<String, Boolean> sendSyncListRequest(String IP){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IP+":8081/ProjectY/Client/SyncAgent/sendSyncListRequest"))
                .build();
        try {
            HttpResponse<String> stringResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JSONObject response = mapper.readValue(stringResponse.body(),JSONObject.class);
            ArrayList<String> keys = (ArrayList<String>) response.get("Keys");
            ArrayList<Boolean> values = (ArrayList<Boolean>) response.get("Values");
            HashMap<String,Boolean> list = new HashMap<>();
            for(int i=0;i< keys.size();i++){
                list.put(keys.get(i),values.get(i));
            }
            return list;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * -------------
     * FAILURE AGENT
     * -------------
     */
    public void sendFailureAgent(String IP, JSONObject message){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IP+":8080/ProjectY/Client/FailureAgent"))
                .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                .header("Content-type", "application/json")
                .timeout(Duration.ofSeconds(1000))
                .build();
        try {
            client.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
