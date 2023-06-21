package ProjectY.Client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Vector;

public class FileLog {
    private final String fileName;
    private final int fileID;
    private int owner;
    private String ownerIP;
    private String replicatedOwner;
    public FileLog(String fileName, int fileID) {
        this.fileName = fileName;
        this.fileID = fileID;
    }

    public int getFileID() {return fileID;}
    public String getFileName(){
            return fileName;
        }
    public int getOwner() {
    return owner;
}
    public String getOwnerIP() {
        return ownerIP;
    }
    public String getReplicatedOwner() {
    return replicatedOwner;
}


    public void setOwner(int ownerID) {this.owner = ownerID;}
    public void setOwnerIP(String ownerIP) {
    this.ownerIP = ownerIP;
}
    public void setReplicatedOwner(String replicatedOwner) {
    this.replicatedOwner = replicatedOwner;
}

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
