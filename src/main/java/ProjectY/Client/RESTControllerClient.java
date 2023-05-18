package ProjectY.Client;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "ProjectY")

public class RESTControllerClient {

    @PostMapping(path = "Discovery")
    public void discoveryRespons(@RequestBody JSONObject response) {
        ClientService clientService = new ClientService();
        System.out.println("Discovery Response received");
        System.out.println(response);
        clientService.handleDiscovery((String) response.get("Name"));
    }

    @PutMapping(path = "Shutdown/{nodeName}/{IPAddress}")
    public void shutdown(@PathVariable("nodeName") String nodeName, @PathVariable("IPAddress") String IPAddress) throws IOException, InterruptedException {
        ClientService clientService = new ClientService();
        ClientApplication.client.shutdown();
    }

/*
    @PutMapping(path = "Update/{nodeName}")
    public JSONObject update(@PathVariable("nodeName") String nodeName) {
        ClientService clientService = new ClientService(this.client);
        return clientService.update(nodeName);
    }
*/

    @PutMapping("Update/PreviousNode/{NextId}")
    public void updatePreviousNode(@PathVariable("NextId") int NextId) {
        ClientService clientService = new ClientService();
        ClientApplication.client.setNextId(NextId);
    }

    @PutMapping("Update/NextNode/{PreviousId}")
    public void updateNextNode(@PathVariable("Previous") int PreviousId) {
        ClientService clientService = new ClientService();
        ClientApplication.client.setPreviousId(PreviousId);
    }


    @PutMapping(path = "Replication")
    public JSONObject replication(@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        return clientService.handleReplication(message);
    }
}
