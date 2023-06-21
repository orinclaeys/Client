package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import ProjectY.HttpComm.TcpModule;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class FailureAgent implements Runnable, Serializable {
    private int currentID;
    private int failingID;
    private boolean isRunning = true;
    private HttpModule httpModule = new HttpModule();
    private TcpModule tcpModule = new TcpModule();

    public FailureAgent(int currentID, int failingID) {
        this.currentID = currentID;
        this.failingID = failingID;
    }

    @Override
    public void run() {
        //Timer timer = new Timer();
        //timer.schedule(new TimerTask() {
            //@Override
            //public void run() {
        while(isRunning) {
            // If the node id is equal to the node id that started the agent -> terminate the Agent
            if (currentID == ClientApplication.client.getCurrentId()) {
                //timer.cancel();
                isRunning = false;
            } else {
                if (ClientApplication.client.getCurrentId() != failingID) {
                    // Get the IP of the node
                    //String IP = ClientApplication.client.getIPAddres();

                    // Get the files names from the node where the failing ID is the owner
                    Vector<String> failureFileNameList = ClientApplication.client.getFailureFileNameList(failingID);

                    // If the failure node the owner -> transfer file
                    for (String fileName : failureFileNameList) {
                        // If the file is already stored on the new owner, only the log should be updated
                        // If the current node is already transferred-> update download location
                        if (ClientApplication.client.isFileTransferred(fileName)) {
                            ClientApplication.client.setNewOwner(fileName);

                        }
                        // If the new owner doesn’t have a copy of this file already,
                        // then the file transfer can be done without any problems.
                        // The logs should be updated with a new download location.
                        // else -> file transfer and update the download location
                        else {
                            ClientApplication.client.setNewOwner(fileName);
                            // REPLICATED OWNER

                            // Get the IP of the failing node
                            String failingIP = httpModule.sendIPRequest(currentID);
                            tcpModule.sendFile(failingID, failingIP, ClientApplication.client.getIPAddres(), fileName);
                        }
                    }
                }
            }
        }
            //}
        //}, 0, 5000);
    }

}

