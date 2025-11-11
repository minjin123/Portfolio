package springbook.chatbotserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
/*
 * ChatbotServerApplication.java
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
@EnableCaching
public class ChatbotServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbotServerApplication.class, args);
	}

}
