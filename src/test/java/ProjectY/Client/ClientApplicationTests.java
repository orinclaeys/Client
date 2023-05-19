package ProjectY.Client;

import ProjectY.HttpComm.TcpModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Vector;

@SpringBootTest
class ClientApplicationTests {

	@Test
	void replication() {
		Client client1 = new Client();

	}
	@Test
	void removeOwner() {
		FileLog fileLog = new FileLog("test", 123);
		fileLog.addReplicatedOwner("1");
		fileLog.addReplicatedOwner("2");
		fileLog.addReplicatedOwner("3");
		System.out.println(fileLog.getReplicatedOwners());
		fileLog.updateReplicatedOwner("2", "8");
		System.out.println(fileLog.getReplicatedOwners());
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
		tcpModule.sendFile("192.168.1.1","abc.txt");
	}

}
