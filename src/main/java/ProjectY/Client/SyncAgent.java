package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Sync agents has to make sure that all available files in the network are at the right place,
 * checking every node and all the files this node owns.
 */

public class SyncAgent implements Runnable, Serializable {
    private HttpModule httpModule = new HttpModule();
    private Map<String, Boolean> newList = new HashMap<>();
    private Map<String, Boolean> oldList = new HashMap<>();
    private Map<String, Boolean> syncList = new HashMap<>();

    @Override
    public void run() {
        while(true) {
            // The new list is equal to the list of the nodes that the node owns
            newList = ClientApplication.client.getOwnerList();

            // Get the IP of the next node
            String nextIP = httpModule.sendIPRequest(ClientApplication.client.getNextId());

            // The list is equal to the sync list of the next node
            syncList = httpModule.sendSyncListRequest(nextIP);

            for (String fileName : oldList.keySet()) {
                // Check if the new list doest not contain the file name from the old list
                if (!newList.containsKey(fileName)) {
                    // Remove the file from the sync list
                    syncList.remove(fileName);
                }
            }

            for (String fileName : newList.keySet()) {
                // Check if the old list does not contain the file name from the new list
                if (!oldList.containsKey(fileName)) {
                    // Add the file name to the sync list
                    syncList.put(fileName, newList.get(fileName));
                }

                // Update the lock value
                // true = locked
                // false = unlocked
                syncList.replace(fileName, newList.get(fileName));
            }

            // The old list becomes the new list
            oldList = new HashMap<>(newList);

            // Update the list stored by the node based on the agent’s list
            ClientApplication.client.setSyncList(syncList);

            try {
                sleep(10000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
