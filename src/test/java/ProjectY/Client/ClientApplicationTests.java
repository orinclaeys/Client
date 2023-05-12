package ProjectY.Client;

import ProjectY.HttpComm.HttpModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClientApplicationTests {

	@Test
	public void HttpModuleTest() throws Exception{
		HttpModule httpModule = new HttpModule();
		System.out.println(httpModule.sendIPRequest(1));
		System.out.println(httpModule.sendIPRequest(5));
		System.out.println(httpModule.sendIPRequest(7));
	}

}
