package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;

import java.io.File;
import java.util.*;

public class SyncAgent {

    private List<String> fileListOld;
    private List<String> fileListNew;
    private List<String> fileList;
    private HttpModule httpModule = new HttpModule();
    public void updateList(String IP, Vector<String> fileVector) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fileListNew = fileVector;
                // The file list is equal to the file list of the next node
                fileList = httpModule.getFileList(IP);
                // for testing:
                //fileList = Arrays.asList("a", "b", "c");
                //System.out.println("fileList : "+fileList);
                // The new list does not contain the old file name -> remove the file name from the list
                for (String fileName : fileListOld) {
                    if (!fileListNew.contains(fileName)) {
                        fileList.remove(fileName);
                    }
                }
                // The old list does not contain the new file name -> add the file name to the list
                for (String fileName : fileListNew) {
                    if (!fileListOld.contains(fileName)) {
                        fileList.add(fileName);
                    }
                }
                // The new list becomes the old list
                fileListOld = fileListNew;
            }
        }, 0, 5000);
    }


    public List<String> getFileList() {
        return fileList;
    }
}
