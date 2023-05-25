package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Vector;


public class ClientService extends Thread {

    private final Client client = ClientApplication.client;
    private HttpModule httpModule = new HttpModule();

    public ClientService() {}

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
    public void handleDiscoveryResponse(JSONObject message){
        if(message.get("Sender").equals("Client")){
            System.out.println("Message received form Client");
            if(message.get("Update").equals(true)){
                if((Integer) message.get("YourPreviousID")>0) {
                    this.client.setPreviousId((Integer) message.get("YourPreviousID"));
                }
                if((Integer) message.get("YourNextID")>0) {
                    this.client.setNextId((Integer) message.get("YourNextID"));
                }
            }
        }
        if(message.get("Sender").equals("NamingServer")){
            System.out.println("Message received from Server");
            if(message.get("Size").equals(1)){
                this.client.NodeType="FirstNode";
                this.client.setNextId(this.client.getCurrentId());
                this.client.setPreviousId(this.client.getCurrentId());
                System.out.println("First node in the network");
            }
        }
        client.updateNodeType();
    }

    public JSONObject handleReplication(JSONObject message) {
        JSONObject response = new JSONObject();
        if (message.get("Sender").equals("Client")){
            if (message.get("Message").equals("Replication")){
                FileLog fileLog = (FileLog) message.get("Filelog");
                client.replication(fileLog,(String) message.get("IP"));
            }
        }
        return response;
    }

    public void handleFailureResponse(JSONObject response){
        System.out.println("Client: Handle failure response");
        if(response.get("nextId").equals(client.getCurrentId())){
            client.setPreviousId((int) response.get("PreviousId"));
            System.out.println("PreviousId updated on next node");
        }
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
            System.out.println("Client: handle file information");
            int portNumber = (int) message.get("PortNumber");
            String filename = (String) message.get("Filename");

            tcpModule.receiveFile(portNumber, filename);
        }
    }

    public void handleDeleteFile(String fileName) {
        client.deleteFile(fileName);
    }

    public JSONObject handleSync(){
        JSONObject response = new JSONObject();
        JSONArray fileArray = new JSONArray();
        fileArray = (JSONArray) client.syncAgent.getFileList();
        response.put("List", fileArray);
        return response;
    }
}
