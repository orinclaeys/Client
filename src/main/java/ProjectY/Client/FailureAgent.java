package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

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
            // Get the filenames from the node where the failing ID is the owner
            Vector<String> failureFileNameList = ClientApplication.client.getFailureFileNameList(failingID);

            // If the failure node the owner -> transfer file
            for (String fileName : failureFileNameList) {
                // If the file is already stored on the new owner, only the log should be updated
                // If the current node is already transferred-> update download location
                if (ClientApplication.client.hasFile(fileName)) {
                    //Change the log
                    ClientApplication.client.setNewOwner(fileName);
                    //find new replicator
                    JSONObject message = new JSONObject();
                    message.put("Sender", "Client");
                    message.put("Message", "Replication");
                    message.put("fileID",ClientApplication.client.Hash(fileName));
                    JSONObject response = httpModule.sendReplication(message);
                    String replicatedOwnerIP = (String) response.get("ReplicatedOwnerIP");
                    //Send file to new replicator
                    if(!Objects.equals(replicatedOwnerIP, ClientApplication.client.getIPAddres())) {
                        FileLog fileLog = ClientApplication.client.getFileLog(fileName);
                        ClientApplication.client.replication(fileLog, replicatedOwnerIP);
                    }
                }
                // If the new owner doesn’t have a copy of this file already,
                // then the file transfer can be done without any problems.
                // The logs should be updated with a new download location.
                // else -> file transfer and update the download location
                else {
                    ClientApplication.client.setNewOwner(fileName);
                    //find new replicator
                    JSONObject message = new JSONObject();
                    message.put("Sender", "Client");
                    message.put("Message", "Replication");
                    message.put("fileID",ClientApplication.client.Hash(fileName));
                    JSONObject response = httpModule.sendReplication(message);
                    String replicatedOwnerIP = (String) response.get("ReplicatedOwnerIP");
                    //Send file to new replicator
                    if(!Objects.equals(replicatedOwnerIP, ClientApplication.client.getIPAddres())) {
                        FileLog fileLog = ClientApplication.client.getFileLog(fileName);
                        ClientApplication.client.replication(fileLog, replicatedOwnerIP);
                    }
                    // Get the IP of the failing node
                    String failingIP = httpModule.sendIPRequest(currentID);
                    tcpModule.sendFile(failingID, failingIP, ClientApplication.client.getIPAddres(), fileName);
                }
            }
        }
    }
}

