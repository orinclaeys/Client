package ProjectY.Client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

}
