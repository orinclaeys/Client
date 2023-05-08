package ProjectY.HttpComm;

import ProjectY.Client.Client;
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
    private Client node;

    public HttpModule(Client node) {this.node = node;}

    public void sendDiscovery(JSONObject message){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8080/ProjectY/Discovery"))
                    //.uri(URI.create("http://172.30.0.1:8080/ProjectY/Discovery"))
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
            for(int i=0;i<IPlist.size();i++){
                HttpRequest request2 = HttpRequest.newBuilder()
                        .uri(URI.create("http://"+IPlist.get(i)+":8081/ProjectY/Discovery"))
                        .POST(HttpRequest.BodyPublishers.ofString(message.toJSONString()))
                        .header("Content-type", "application/json")
                        .timeout(Duration.ofSeconds(1000))
                        .build();
                System.out.println("Client: Sending to client...");

                HttpResponse<String> Stringresponse2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
                JSONObject response2 = mapper.readValue(Stringresponse.body(),JSONObject.class);
                service.handleDiscoveryRespons(response2);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}

