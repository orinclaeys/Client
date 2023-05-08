package ProjectY.Client;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "ProjectY")

public class RESTControllerClient {

    @PostMapping(path = "Discovery/Response")
    public void discoveryRespons(@RequestBody JSONObject response) {
        ClientService clientService = new ClientService(ClientApplication.client);
        System.out.println("Discovery Response received");
        System.out.println(response);
        clientService.handleDiscoveryRespons(response);
    }

    @PutMapping(path = "Shutdown/{nodeName}/{IPAddress}")
    public void shutdown(@PathVariable("nodeName") String nodeName, @PathVariable("IPAddress") String IPAddress) throws IOException, InterruptedException {
        ClientService clientService = new ClientService(ClientApplication.client);
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
    public void shutdownPreviousNode(@PathVariable("NextId") int NextId) {
        ClientService clientService = new ClientService(ClientApplication.client);
        ClientApplication.client.setNextId(NextId);
    }

    @PutMapping("Update/NextNode/{PreviousId}")
    public void shutdownNextNode(@PathVariable("Previous") int PreviousId) {
        ClientService clientService = new ClientService(ClientApplication.client);
        ClientApplication.client.setPreviousId(PreviousId);
    }

    @PutMapping(path = "Failure/{nodeName}")
    public void failure(@PathVariable("nodeName") String nodeName) throws IOException, InterruptedException {
        ClientService clientService = new ClientService(ClientApplication.client);
        ClientApplication.client.failure(nodeName);
    }

}
