package Krisseldu.Krisseldu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;  // ⬅️ IMPORTANTE

@SpringBootApplication
@EnableScheduling            // ⬅️ Activa los métodos @Scheduled
public class KrisselduApplication {

	public static void main(String[] args) {
		SpringApplication.run(KrisselduApplication.class, args);
	}
}
