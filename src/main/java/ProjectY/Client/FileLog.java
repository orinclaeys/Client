package ProjectY.Client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Vector;

public class FileLog {
        private String fileName;
        private int fileID;
        private int owner;
        private String ownerIP;
        private String replicatedOwner;
        public FileLog(String fileName, int fileID) {
            this.fileName = fileName;
            this.fileID = fileID;
        }
        public void setOwner(int ownerID) {this.owner = ownerID;}

        public String getReplicatedOwner() {
            return replicatedOwner;
        }

        public void setReplicatedOwner(String replicatedOwner) {
            this.replicatedOwner = replicatedOwner;
        }

        public int getOwner() {
                return owner;
            }
        public String getFileName(){
                return fileName;
            }

        public void setOwnerIP(String ownerIP) {
                this.ownerIP = ownerIP;
            }

        public String getOwnerIP() {
            return ownerIP;
        }


        public int getFileID() {return fileID;}

        @Override
        public String toString() {
            return "FileLog{" +
                    "fileName='" + fileName + '\'' +
                    ", fileID=" + fileID +
                    ", owner=" + owner +
                    ", ownerIP=" + ownerIP +
                    ", replicatedOwner=" + replicatedOwner +
                    '}';
        }
}
