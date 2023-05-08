package ProjectY.Client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClientApplicationTests {

	@Test
	void contextLoads() {
		Client client = new Client("TestClient");
		client.Discovery();
	}

}
