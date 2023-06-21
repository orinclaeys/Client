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
    private int ID;
    private boolean isRunning = true;
    private HttpModule httpModule = new HttpModule();
    private TcpModule tcpModule = new TcpModule();
    private Vector<Integer> idList = new Vector<Integer>();
    public FailureAgent(int currentID, int failingID) {
        this.currentID = currentID;
        this.failingID = failingID;
        this.ID = currentID;
    }

    @Override
    public void run() {
        //Timer timer = new Timer();
        //timer.schedule(new TimerTask() {
            //@Override
            //public void run() {
        while(isRunning) {
            // If the node id is equal to the node id that started the agent -> terminate the Agent
            if (idList.contains(currentID)) {
                //timer.cancel();
                isRunning = false;
            } else {
                idList.add(ID);
                if (ID != failingID) {
                    // Get the IP of the node
                    String IP = httpModule.sendIPRequest(ID);

                    // Get the files names from the node where the failing ID is the owner
                    Vector<String> failureFileNameList = httpModule.sendFailureFileNameListRequest(IP, failingID);

                    // If the failure node the owner -> transfer file
                    for (String fileName : failureFileNameList) {
                        // If the file is already stored on the new owner, only the log should be updated
                        // If the current node is already transferred-> update download location
                        if (httpModule.sendIsFileTransferredRequest(IP, fileName)) {
                            httpModule.sendNewOwner(IP, fileName);
                        }
                        // If the new owner doesn’t have a copy of this file already,
                        // then the file transfer can be done without any problems.
                        // The logs should be updated with a new download location.
                        // else -> file transfer and update the download location
                        else {
                            httpModule.sendNewOwner(IP, fileName);

                            // Get the IP of the failing node
                            String failingIP = httpModule.sendIPRequest(currentID);
                            tcpModule.sendFile(failingIP, IP, fileName);
                        }
                    }
                }
                ID = httpModule.sendNextIDRequest(ID);
            }
        }
            //}
        //}, 0, 5000);
    }

    // If the node id is equal to the node id that started the agent -> terminate the Agent
    public Boolean terminate() {
        Boolean terminate = false;
        if (idList.contains(ID)) {
            terminate = true;
        }
        return terminate;
    }
}

