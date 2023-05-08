package ProjectY.HttpComm;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        JSONObject message = new JSONObject();
        message.put("test",true);
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
    }
}
