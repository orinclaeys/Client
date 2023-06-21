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
    public void updateNextNode(@PathVariable("PreviousId") int PreviousId) {
        ClientService clientService = new ClientService();
        ClientApplication.client.setPreviousId(PreviousId);
        ClientApplication.client.updateNodeType();
    }

    @PutMapping(path = "Replication")
    public JSONObject replication(@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        return clientService.handleReplication(message);
    }

    @PostMapping(path = "Client/replication/sendFileInformation")
    public void receiveFileInformation (@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        clientService.handleFileInformation(message);
    }

    @DeleteMapping(path="Client/replication/sendDeleteFile/{fileName}")
    public void deleteFile(@PathVariable("fileName") String fileName) {
        ClientService clientService = new ClientService();
        System.out.println("Delete "+fileName);
        clientService.handleDeleteFile(fileName);
    }
    @GetMapping(path="Client/Discovery/askReplicationFiles/{newNode}/{newNodeIP}")
    public void askReplicationFiles(@PathVariable("newNode") String newNode,@PathVariable("newNodeIP") String newNodeIP){
        System.out.println("New node asking for replication files...");
        ClientApplication.client.askReplicationFiles(newNode,newNodeIP);
        System.out.println("Handled replication request");
    }

    @GetMapping(path = "Client/SyncAgent")
    public JSONObject sendSync(){
        ClientService clientService = new ClientService();
        return clientService.handleSync();
    }
    @PutMapping(path="Client/replication/update/{fileName}/{replicationIP}")
    public void updateReplicationIP(@PathVariable("fileName") String fileName,@PathVariable("replicationIP") String replicationIP){
        System.out.println("Updating fileInformation");
        ClientApplication.client.updateReplicatedIP(fileName,replicationIP);
        System.out.println("fileInformation updated");
    }
    @GetMapping(path="Client/replication/sendFile/{fileName}")
    public void sendFile(@PathVariable("fileName") String fileName){
        System.out.println("Sending file "+fileName);
        ClientApplication.client.getFile(fileName);
        System.out.println("File send");
    }
    @PutMapping(path="Client/replication/resetFile/{fileName}")
    public void resetFile(@PathVariable("fileName") String fileName){
        System.out.println("Sending file "+fileName);
        ClientApplication.client.updateReplicatedIP(fileName,null);
        System.out.println("File send");
    }
}
