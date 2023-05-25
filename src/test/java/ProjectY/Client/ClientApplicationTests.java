package ProjectY.Client;

import ProjectY.HttpComm.TcpModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

@SpringBootTest
class ClientApplicationTests {

	@Test
	void replication() {
		Client client1 = new Client();
		client1.verifyFiles();

	}


	@Test
	void fileLogList() {
		Client client = new Client();
/*		FileLog fileLog1 = new FileLog("test1", 123);
		FileLog fileLog2 = new FileLog("test2", 5478);
		FileLog fileLog3 = new FileLog("test3", 12823);
		FileLog fileLog4 = new FileLog("test4", 12293);
		Vector<FileLog> fileLogList = new Vector<>();
		fileLogList.add(fileLog1);
		fileLogList.add(fileLog2);
		fileLogList.add(fileLog3);
		fileLogList.add(fileLog4);
		System.out.println(fileLogList);*/
		client.replicationUpdate();

	}

	@Test
	void tcp(){
		TcpModule tcpModule = new TcpModule();
		//tcpModule.sendFile("localhost","abc.txt");
		//tcpModule.receiveFile(5006,"sentThisFile.txt");
	}

	@Test
	void deleteFile(){
		Client client = new Client();
		client.deleteFile("abc.txt");

	}

//	@Test
//	void test(){
//		SyncAgent syncAgent = new SyncAgent();
//		Vector<String> fileListNew;
//		syncAgent.updateList(9, fileListNew);
//
//	}

}
