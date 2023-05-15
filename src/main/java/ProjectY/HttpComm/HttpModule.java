package ProjectY.HttpComm;

import ProjectY.Client.Client;
import ProjectY.Client.ClientApplication;
import ProjectY.Client.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Vector;

public class HttpModule{
    private final Client node = ClientApplication.client;
    private final static String serverIP="172.30.0.5";

    public HttpModule() {}

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
            System.out.println("Client: Sending to server...");

            HttpResponse<String> Stringresponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JSONObject response = mapper.readValue(Stringresponse.body(),JSONObject.class);
            System.out.println("Client: Server response: "+response.toJSONString());
            ClientService service = new ClientService(node);
            service.handleDiscoveryRespons(response);
            Vector<String> IPlist = new Vector<String>((ArrayList<String>) response.get("IPlist"));
            for (String ipAddr : IPlist) {
                HttpRequest request2 = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + ipAddr + ":8081/ProjectY/Discovery"))
                        .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(1000))
                        .build();
                System.out.println("Client: Sending to client...");

                HttpResponse<String> Stringresponse2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
                JSONObject response2 = mapper.readValue(Stringresponse2.body(), JSONObject.class);
                service.handleDiscoveryRespons(response2);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
    public void sendReplication(JSONObject message){}
    public String sendIPRequest(int ID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ProjectY/NamingServer/getIPAddress/"+ ID))
                //.uri(URI.create("http://"+serverIP+":8080/ProjectY/NamingServer/getIPAddress/"+ ID))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    public void sendFailure(JSONObject message){System.out.println("HttpModule: sendReplication: " + message);}
    public void sendUpdatePreviousNode(String IPAddress, int nextID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest requestPreviousNode = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IPAddress+":8081/ProjectY/Update/PreviousNode/"+ nextID))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(requestPreviousNode, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendUpdateNextNode(String IPAddress, int previousID){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest requestPreviousNode = HttpRequest.newBuilder()
                .uri(URI.create("http://"+IPAddress+":8081/ProjectY/Update/NextNode/"+ previousID))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(requestPreviousNode, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendShutdown(String name){
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest requestDeleteNode = HttpRequest.newBuilder()
                //.uri(URI.create("http://localhost:8080/ProjectY/NamingServer/deleteNode"+name))
                .uri(URI.create("http://"+serverIP+":8080/ProjectY/NamingServer/deleteNode"+name))
                .build();
        try {
            HttpResponse<String> responseDeleteNode = httpClient.send(requestDeleteNode, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}

