package ProjectY.Client;

import org.json.simple.JSONObject;

import java.util.Vector;

public class FileLog {
        private String fileName;
        private int fileID;
        private int owner;
        private Vector<String> replicatedOwners;
        private Vector<String> downloadLocations;

        public FileLog(String fileName, int fileID) {
            this.fileName = fileName;
            this.fileID = fileID;
        }
        public void setOwner(int ownerID) {this.owner = ownerID;}

        public void addReplicatedOwner(String replicatedOwner) {this.replicatedOwners.add(replicatedOwner);}



}
