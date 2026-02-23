package egovframework.com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EgovAdminMainApplication {

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		SpringApplication.run(EgovAdminMainApplication.class, args);
	}

}
