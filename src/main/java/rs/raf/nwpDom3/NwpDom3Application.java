package rs.raf.nwpDom3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class NwpDom3Application {

	@Bean
	public static BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder(10);
	}

	public static void main(String[] args) {
		new BCryptPasswordEncoder(10);
		SpringApplication.run(NwpDom3Application.class, args);
	}

}
