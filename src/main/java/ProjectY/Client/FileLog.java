package ProjectY.Client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Vector;

public class FileLog {
        private String fileName;
        private int fileID;
        private int owner;
        private Vector<Integer> replicatedOwners;
        private Vector<String> downloadLocations;

        public FileLog(String fileName, int fileID) {
            this.fileName = fileName;
            this.fileID = fileID;
        }
        public void setOwner(int ownerID) {this.owner = ownerID;}

        public JSONObject toJSON(){
            JSONObject response = new JSONObject();
            response.put("fileName",fileName);
            response.put("fileID",fileID);
            response.put("owner",owner);
            JSONArray replicatedOwnerJSON = new JSONArray();
            for(int i = 0;i<replicatedOwners.size();i++){
                replicatedOwnerJSON.add(i,replicatedOwners.get(i));
            }
            response.put("replicatedOwners",replicatedOwnerJSON);
            JSONArray downloadLocationsJSON = new JSONArray();
            for(int i = 0;i<downloadLocations.size();i++){
                replicatedOwnerJSON.add(i,downloadLocations.get(i));
            }
            response.put("downloadLocation",downloadLocationsJSON);
            return response;
        }

    }
