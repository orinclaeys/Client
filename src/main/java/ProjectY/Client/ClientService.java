package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;


public class ClientService extends Thread {

    private final Client client = ClientApplication.client;

    public ClientService() {}

    /**
     * ---------
     * DISCOVERY
     * ---------
     */

    /**
     * Handles discovery: Auto-discover the Naming server and existing nodes in the network
     * Starting each node, initializing local parameters (previous, nextnode), and updating
     * parameters of existing nodes
     *
     * @param name the name of the node
     * @return response if the update succeeded and the next id and the previous id
     */
    public JSONObject handleDiscovery(String name) {
        JSONObject response = new JSONObject();
        response.put("Sender","Client");
        boolean nextIDUpdated = client.updateNextID(name);
        boolean previousIDUpdated = client.updatePreviousID(name);
        if(this.client.NodeType=="FirstNode"){
            response.put("Update",true);
            response.put("YourNextID",client.getCurrentId());
            response.put("YourPreviousID",client.getCurrentId());
        }else{
            if (nextIDUpdated) {
                response.put("Update", true);
                response.put("YourPreviousID", client.getCurrentId());
                response.put("YourNextID", -1);
            } else if (previousIDUpdated) {
                response.put("Update", true);
                response.put("YourNextID", client.getCurrentId());
                response.put("YourPreviousID", -1);
            } else {
                response.put("Update", false);
            }
        }
        this.client.updateNodeType();

        return response;
    }

    /**
     * Handles discovery response: Auto-discover the Naming server and existing nodes in the network
     * A node that sent multicast message receives response from Naming server:
     *
     * @param message the received message
     */
    public void handleDiscoveryResponse(JSONObject message){
        if(message.get("Sender").equals("Client")){
            //System.out.println("Message received form Client");
            if(message.get("Update").equals(true)){
                if((Integer) message.get("YourPreviousID")>0) {
                    this.client.setPreviousId((Integer) message.get("YourPreviousID"));
                }
                if((Integer) message.get("YourNextID")>0) {
                    this.client.setNextId((Integer) message.get("YourNextID"));
                }
            }
        }
        //If the number of existing nodes in the network is < 1,
        // it means that this is the only node in the network
        // (this node is its previous and next node, previousID = currentID, nextID = currentID)
        if(message.get("Sender").equals("NamingServer")){
            //System.out.println("Message received from Server");
            if(message.get("Size").equals(1)){
                this.client.NodeType="FirstNode";
                this.client.setNextId(this.client.getCurrentId());
                this.client.setPreviousId(this.client.getCurrentId());
            }
        }
        client.updateNodeType();
    }
    public void handleShutdown() {
        client.shutdown();
    }
    public void  handleUpdateNextNode(int PreviousId){
        client.setPreviousId(PreviousId);
        client.updateNodeType();
    }
    public void  handleUpdatePreviousNode(int NextId){
        client.setNextId(NextId);
        client.updateNodeType();
    }

    /**
     * -----------
     * REPLICATION
     * -----------
     */
    public void handleDeleteFile(String fileName) {
        client.deleteFile(fileName);
    }
    public void handleFailureResponse(JSONObject response){
        // Update the `next node` parameter of the previous node
        // with the information received from the nameserver
        //System.out.println("Client: Handle failure response");
        if(response.get("nextId").equals(client.getCurrentId())){
            client.setPreviousId((int) response.get("PreviousId"));
            System.out.println("PreviousId updated on next node");
        }
        // Update the `previous node` parameter of the next node
        // with the information received from the nameserver
        else if (response.get("previousId").equals(client.getCurrentId())){
            client.setNextId((int) response.get("NextId"));
            System.out.println("NextId updated on previous node");
        }
        else
            System.out.println("Nothing updated!");
    }

    public void handleFileInformation(JSONObject message){
        TcpModule tcpModule = new TcpModule();

        if(message.get("DestinationAddress").equals(client.getIPAddres())) {
            //System.out.println("Client: handle file information");
            int portNumber = (int) message.get("PortNumber");
            String filename = (String) message.get("Filename");
            String ownerIP = (String) message.get("ownerIP");
            int ownerID = (Integer) message.get("ownerID");
            ClientApplication.client.addReplicatedFile(filename,ownerIP,ownerID);
            tcpModule.portnumber = portNumber;
            tcpModule.Filename = filename;
            Thread thread = new Thread(tcpModule);
            thread.start();
        }
    }
    public JSONObject handleReplication(JSONObject message) {
        JSONObject response = new JSONObject();
        if (message.get("Sender").equals("Client")){
            if (message.get("Message").equals("Replication")){
                FileLog fileLog = (FileLog) message.get("FileLog");
                client.replication(fileLog, (String) message.get("IP"));
            }
        }
        return response;
    }


    public JSONObject handleSyncListRequest() {
        JSONObject response = new JSONObject();
        System.out.println("Sending list: "+client.getSyncList());
        response.put("Keys", client.getSyncList().keySet().toArray());
        response.put("Values",client.getSyncList().values().toArray());
        return response;
    }

    /**
     * ----------
     * SYNC AGENT
     * ----------
     */
    public JSONObject handleOwnerListRequest(){
        JSONObject response = new JSONObject();
        String jsonStr = JSONValue.toJSONString(client.getOwnerList());
        response.put("OwnerList", jsonStr);
        return response;
    }

    /**
     * -------------
     * FAILURE AGENT
     * -------------
     */
    public JSONObject handleFailureFileNameList(int failureID){
        JSONObject response = new JSONObject();
        Vector<String> failureFileNameList = client.getFailureFileNameList(failureID);
        response.put("FailureFileNameList", failureFileNameList);
        return response;
    }

    public void handleNewOwner(String fileName){
        client.setNewOwner(fileName);
    }

    public void handleFailureAgent(JSONObject message){
        int currentID = (int) message.get("CurrentID");
        int failingID  = (int) message.get("FailingID");
        ClientApplication.client.startFailureAgent(currentID,failingID);
    }
}
