package springbook.chatbotserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
/*
 * ChatbotServerApplication.java
 */
@SpringBootApplication
@EnableScheduling
public class ChatbotServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotServerApplication.class, args);
	}

}
