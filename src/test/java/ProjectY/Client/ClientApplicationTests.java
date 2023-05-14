package ProjectY.Client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ClientApplicationTests {

	@Test
	void testShutdown() throws IOException, InterruptedException {
		Client client1 = new Client("Node1");
		Client client2 = new Client();
		Client client3 = new Client();

		client2.shutdown();
	}

}
