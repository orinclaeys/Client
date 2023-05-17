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
        public Vector<String> getReplicatedOwners(){
            return this.replicatedOwners;
        }
        public int getOwner() {
            return owner;
        }

        public String getFileName(){
            return fileName;
        }

        public void updateReplicatedOwner(String oldReplicatedOwner, String newReplicatedOwner) {
            replicatedOwners.remove(oldReplicatedOwner);
            addReplicatedOwner(newReplicatedOwner);
        }

        public int getFileID() {return fileID;}

        public Vector<String> getDownloadLocations() {return downloadLocations;}
}
