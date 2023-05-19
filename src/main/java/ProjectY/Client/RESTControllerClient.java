package ProjectY.Client;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "ProjectY")

public class RESTControllerClient {

    @PostMapping(path = "Discovery")
    public JSONObject discoveryResponse(@RequestBody JSONObject response) {
        ClientService clientService = new ClientService();
        System.out.println("Discovery received");
        System.out.println(response);
        return clientService.handleDiscovery((String) response.get("Name"));
    }

    @PutMapping(path = "Shutdown/{nodeName}/{IPAddress}")
    public void shutdown(@PathVariable("nodeName") String nodeName, @PathVariable("IPAddress") String IPAddress) throws IOException, InterruptedException {
        ClientService clientService = new ClientService();
        ClientApplication.client.shutdown();
    }

    @PutMapping("Update/PreviousNode/{NextId}")
    public void updatePreviousNode(@PathVariable("NextId") int NextId) {
        ClientService clientService = new ClientService();
        ClientApplication.client.setNextId(NextId);
        ClientApplication.client.updateNodeType();
    }

    @PutMapping("Update/NextNode/{PreviousId}")
    public void updateNextNode(@PathVariable("Previous") int PreviousId) {
        ClientService clientService = new ClientService();
        ClientApplication.client.setPreviousId(PreviousId);
        ClientApplication.client.updateNodeType();
    }

    @PutMapping(path = "Replication")
    public JSONObject replication(@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        return clientService.handleReplication(message);
    }

    @PutMapping(path = "Client/replication/sendFileInformation")
    public void receiveFileInformation (@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        clientService.handleFileInformation(message);
    }
}
