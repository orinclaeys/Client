package ProjectY.Client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Vector;

public class FileLog {
        private String fileName;
        private int fileID;
        private int owner;
        private Vector<String> replicatedOwners = new Vector<>();
        private Vector<String> downloadLocations = new Vector<>();

        public FileLog(String fileName, int fileID) {
            this.fileName = fileName;
            this.fileID = fileID;
        }
        public void setOwner(int ownerID) {this.owner = ownerID;}

        public void addReplicatedOwner(String replicatedOwner) {this.replicatedOwners.add(replicatedOwner);}

        public JSONObject toJSON(){
            JSONObject response = new JSONObject();
            response.put("fileName",fileName);
            response.put("fileID",fileID);
            response.put("owner",owner);
            JSONArray replicatedOwnerJSON = new JSONArray();
            replicatedOwnerJSON.addAll(replicatedOwners);
            response.put("replicatedOwners",replicatedOwnerJSON);
            JSONArray downloadLocationsJSON = new JSONArray();
            replicatedOwnerJSON.addAll(downloadLocations);
            response.put("downloadLocation",downloadLocationsJSON);
            return response;
        }

    }
