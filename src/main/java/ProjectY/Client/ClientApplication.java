package ProjectY.Client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientApplication {
    public static Client client = new Client();

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

}
