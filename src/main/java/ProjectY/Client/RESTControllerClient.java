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
        //System.out.println("Discovery received");
        System.out.println(response);
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
        ClientApplication.client.askReplicationFiles(newNode,newNodeIP);
    }
    @DeleteMapping(path="Client/replication/sendDeleteFile/{fileName}")
    public void deleteFile(@PathVariable("fileName") String fileName) {
        ClientService clientService = new ClientService();
        clientService.handleDeleteFile(fileName);
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

    @GetMapping(path = "Client/SyncAgent/sendSyncListRequest")
    public JSONObject sendSyncListRequest() {
        //System.out.println("Requesting SyncList");
        ClientService clientService = new ClientService();
        //System.out.println("SyncList sent ");
        return clientService.handleSyncListRequest();
    }

    /**
     * ----------
     * SYNC AGENT
     * ----------
     */
    @PutMapping(path = "Client/SyncAgent/sendOwnerListRequest")
    public JSONObject sendOwnerListRequest(){
        ClientService clientService = new ClientService();
        return clientService.handleOwnerListRequest();
    }


    @PostMapping(path="Client/FailureAgent")
    public void failureAgent(@RequestBody JSONObject message){
        ClientService clientService = new ClientService();
        clientService.handleFailureAgent(message);
    }

    /**
     * -------------
     * FAILURE AGENT
     * -------------
    */
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
    @PutMapping(path="Client/replication/update/{fileName}/{replicationIP}")
    public void updateReplicationIP(@PathVariable("fileName") String fileName,@PathVariable("replicationIP") String replicationIP){
        //System.out.println("Updating fileInformation");
        ClientApplication.client.updateReplicatedIP(fileName,replicationIP);
        //System.out.println("fileInformation updated");
    }
    @GetMapping(path="Client/replication/sendFile/{fileName}")
    public void sendFile(@PathVariable("fileName") String fileName){
        //System.out.println("Sending file "+fileName);
        ClientApplication.client.getFile(fileName);
        //System.out.println("File send");
    }
    @PutMapping(path="Client/replication/resetFile/{fileName}")
    public void resetFile(@PathVariable("fileName") String fileName){
        //System.out.println("resetting file "+fileName);
        ClientApplication.client.updateReplicatedIP(fileName,null);
        //System.out.println("File reset");
    }
}
