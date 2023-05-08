package ProjectY.Client;

import org.json.simple.JSONObject;


public class ClientService extends Thread {

    private Client client;

    public ClientService(Client client) {this.client = client;}

    public JSONObject handleDiscovery(String name) {
        JSONObject response = new JSONObject();
        response.put("Sender","Client");
        if (client.updateNextID(name)) {
            response.put("Update", true);
            response.put("YourPreviousID", client.getCurrentId());
            response.put("YourNextID", client.getNextId());
        }
        else if (client.updatePreviousID(name)) {
            response.put("Update", true);
            response.put("YourNextID", client.getCurrentId());
            response.put("YourPreviousID", client.getPreviousId());
        }
        else {
            response.put("Update", false);
        }
        return response;
    }
    public void handleDiscoveryRespons(JSONObject message){
        if(message.get("Sender").equals("Client")){
            System.out.println("Message received form Client");
            if(message.get("Update").equals(true)){
                this.client.setPreviousId((Integer) message.get("YourPreviousID"));
                this.client.setNextId((Integer) message.get("YourNextID"));
            }
        }
        if(message.get("Sender").equals("NamingServer")){
            System.out.println("Message received from Server");
            if(message.get("Size").equals(1)){
                this.client.setNextId(this.client.getCurrentId());
                this.client.setPreviousId(this.client.getCurrentId());
                System.out.println("First node in the network");
            }
        }
    }



}
