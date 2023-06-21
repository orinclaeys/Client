package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class SyncAgent implements Runnable, Serializable {
    private Map<String, Boolean> oldList = new HashMap<>();
    private Map<String, Boolean> newList = new HashMap<>();
    private Map<String, Boolean> syncList = new HashMap<>();
    private HttpModule httpModule = new HttpModule();

    @Override
    public void run() {
        //Timer timer = new Timer();
        //timer.schedule(new TimerTask() {
            //@Override
            //public void run() {
        while(true) {
            // Listing all files owned by the node at which this agent runs
            // The new list is equal to the list of the nodes that the current node owns
            //newList = httpModule.sendOwnerListRequest(IP);
            newList = ClientApplication.client.getOwnerList();
            System.out.println("Run: newList: " + newList);

            // Get the IP of the next node
            //String nextIP = httpModule.sendPreviousIPRequest(ID);
            String nextIP = httpModule.sendIPRequest(ClientApplication.client.getNextId());
            // The list is equal to the sync list of the next node
            syncList = httpModule.sendSyncListRequest(nextIP);
            System.out.println("Run: syncList: " + syncList);

            // The new list does not contain the old file name -> remove the file name from the list
            for (String fileName : oldList.keySet()) {
                if (!newList.containsKey(fileName)) {
                    syncList.remove(fileName);
                }
            }

            // If one of the owned files is not added to the list, the list needs to be updated
            // The old list does not contain the new file name -> add the file name to the list
            for (String fileName : newList.keySet()) {
                if (!oldList.containsKey(fileName)) {
                    syncList.put(fileName, newList.get(fileName));
                }

                // Update the lock value
                // If there is a lock request on the current node, and the file is not locked on the agent’s list,
                // locking should be enabled on the node and the list should be synchronized accordingly
                // Remove the lock when it is not needed anymore, and update local file list accordingly
                // true = locked
                // false = unlocked
                syncList.replace(fileName, newList.get(fileName));
            }

            // The old list becomes the new list
            oldList = new HashMap<>(newList);
            System.out.println("oldList: " + oldList);
            System.out.println("new syncList: " + syncList);

            // Update the list stored by the node based on the agent’s list
            ClientApplication.client.setSyncList(oldList);
            //}
            // }, 0, 5000);
            try {
                wait(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
