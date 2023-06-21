package ProjectY.Client;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "ProjectY")

public class RESTControllerClient {

    /**
     * ---------
     * DISCOVERY
     * ---------
     */
    @PostMapping(path = "Discovery")
    public JSONObject discoveryResponse(@RequestBody JSONObject response) {
        ClientService clientService = new ClientService();
        return clientService.handleDiscovery((String) response.get("Name"));
    }
    @PutMapping(path = "Shutdown/{nodeName}/{IPAddress}")
    public void shutdown(@PathVariable("nodeName") String nodeName, @PathVariable("IPAddress") String IPAddress) throws IOException, InterruptedException {
        ClientService clientService = new ClientService();
        clientService.handleShutdown();
    }
    @PutMapping("Update/NextNode/{PreviousId}")
    public void updateNextNode(@PathVariable("PreviousId") int PreviousId) {
        ClientService clientService = new ClientService();
        clientService.handleUpdateNextNode(PreviousId);
    }
    @PutMapping("Update/PreviousNode/{NextId}")
    public void updatePreviousNode(@PathVariable("NextId") int NextId) {
        ClientService clientService = new ClientService();
        clientService.handleUpdatePreviousNode(NextId);
    }

    /**
     * -----------
     * REPLICATION
     * -----------
     */
    @GetMapping(path="Client/Discovery/askReplicationFiles/{newNode}/{newNodeIP}")
    public void askReplicationFiles(@PathVariable("newNode") String newNode,@PathVariable("newNodeIP") String newNodeIP){
        ClientService clientService = new ClientService();
        clientService.handleAskReplicationFiles(newNode, newNodeIP);
    }
    @DeleteMapping(path="Client/replication/sendDeleteFile/{fileName}")
    public void deleteFile(@PathVariable("fileName") String fileName) {
        ClientService clientService = new ClientService();
        clientService.handleDeleteFile(fileName);
    }
    @PostMapping(path = "Client/replication/sendFileInformation")
    public void receiveFileInformation (@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        clientService.handleFileInformation(message);
    }
    @PutMapping(path = "Replication")
    public JSONObject replication(@RequestBody JSONObject message) {
        ClientService clientService = new ClientService();
        return clientService.handleReplication(message);
    }
    @PutMapping(path="Client/replication/resetFile/{fileName}")
    public void resetFile(@PathVariable("fileName") String fileName){
        ClientService clientService = new ClientService();
        clientService.handleResetFile(fileName);
    }
    @GetMapping(path="Client/replication/sendFile/{fileName}")
    public void sendFile(@PathVariable("fileName") String fileName){
        ClientService clientService = new ClientService();
        clientService.handleSendFile(fileName);
    }
    @PutMapping(path="Client/replication/update/{fileName}/{replicationIP}")
    public void updateReplicationIP(@PathVariable("fileName") String fileName,@PathVariable("replicationIP") String replicationIP){
        ClientService clientService = new ClientService();
        clientService.handleUpdateReplicationIP(fileName, replicationIP);
    }

    /**
     * ----------
     * SYNC AGENT
     * ----------
     */
    @GetMapping(path = "Client/SyncAgent/sendSyncListRequest")
    public JSONObject sendSyncListRequest() {
        ClientService clientService = new ClientService();
        return clientService.handleSyncListRequest();
    }
    @PutMapping(path = "Client/SyncAgent/sendOwnerListRequest")
    public JSONObject sendOwnerListRequest(){
        ClientService clientService = new ClientService();
        return clientService.handleOwnerListRequest();
    }

    /**
     * -------------
     * FAILURE AGENT
     * -------------
    */
    @PostMapping(path="Client/FailureAgent")
    public void failureAgent(@RequestBody JSONObject message){
        ClientService clientService = new ClientService();
        clientService.handleFailureAgent(message);
    }
    @GetMapping(path = "Client/FailureAgent/sendFailureFileNameListRequest/{failureID}")
    public JSONObject sendFailureFileNameListRequest(@PathVariable("failureID") int failureID){
        ClientService clientService = new ClientService();
        return clientService.handleFailureFileNameList(failureID);
    }
    @PostMapping(path = "Client/FailureAgent/sendNewOwner/{fileName}")
    public void sendNewOwner(@PathVariable("fileName") String fileName){
        ClientService clientService = new ClientService();
        clientService.handleNewOwner(fileName);
    }
}
