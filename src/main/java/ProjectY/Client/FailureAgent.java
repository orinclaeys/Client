package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

/**
 * Failure agent is started as soon as a node failure is detected.
 * The responsibility of this agent is to transfer all the files from
 * a failed node to the new owner, and to update the whole file list.
 */

public class FailureAgent implements Runnable, Serializable {
    private final int currentID;
    private final int failingID;
    private final HttpModule httpModule = new HttpModule();
    private final TcpModule tcpModule = new TcpModule();

    public FailureAgent(int currentID, int failingID) {
        this.currentID = currentID;
        this.failingID = failingID;
    }

    public int getCurrentID(){return currentID;}
    public int getFailingID(){return failingID;}

    @Override
    public void run() {
        if (ClientApplication.client.getCurrentId() != failingID) {

            // Get a list from the node with the file names where the failing ID is the owner
            Vector<String> failureFileNameList = ClientApplication.client.getFailureFileNameList(failingID);

            for (String fileName : failureFileNameList) {
                // Check if file is already stored on new owner
                if (ClientApplication.client.hasFile(fileName)) {
                    // Update the log
                    ClientApplication.client.setNewOwner(fileName);

                    // Find new replicated owner
                    JSONObject message = new JSONObject();
                    message.put("Sender", "Client");
                    message.put("Message", "Replication");
                    message.put("fileID",ClientApplication.client.Hash(fileName));
                    JSONObject response = httpModule.sendReplication(message);
                    String replicatedOwnerIP = (String) response.get("ReplicatedOwnerIP");

                    // Send file to new replicator
                    if(!Objects.equals(replicatedOwnerIP, ClientApplication.client.getIPAddres())) {
                        FileLog fileLog = ClientApplication.client.getFileLog(fileName);
                        ClientApplication.client.replication(fileLog, replicatedOwnerIP);
                    }
                }
                else {
                    // Update the log
                    ClientApplication.client.setNewOwner(fileName);

                    // Find new replicated owner
                    JSONObject message = new JSONObject();
                    message.put("Sender", "Client");
                    message.put("Message", "Replication");
                    message.put("fileID",ClientApplication.client.Hash(fileName));
                    JSONObject response = httpModule.sendReplication(message);
                    String replicatedOwnerIP = (String) response.get("ReplicatedOwnerIP");

                    // Send file to new replicator
                    if(!Objects.equals(replicatedOwnerIP, ClientApplication.client.getIPAddres())) {
                        FileLog fileLog = ClientApplication.client.getFileLog(fileName);
                        ClientApplication.client.replication(fileLog, replicatedOwnerIP);
                    }

                    // Get the IP of the failing node and transfer the file to the new owner
                    String failingIP = httpModule.sendIPRequest(currentID);
                    tcpModule.sendFile(failingID, failingIP, ClientApplication.client.getIPAddres(), fileName);
                }
            }
        }
    }
}

