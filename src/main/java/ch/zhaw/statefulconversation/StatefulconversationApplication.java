package ch.zhaw.statefulconversation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import ch.zhaw.statefulconversation.spi.OpenAIProperties;

/**
 * Einstiegspunkt der Oblivio-Applikation (Spring Boot).
 *
 * <p>Oblivio ist eine digitale Nachlassplattform, die auf dem PROMISE Framework basiert.
 * Die Applikation nutzt Spring Boot mit JPA-Persistenz und OpenAI GPT-4o fuer
 * zustandsgesteuerte Konversationen im Bereich Digital Legacy Planning.
 */
@SpringBootApplication
@EnableConfigurationProperties(OpenAIProperties.class)
public class StatefulconversationApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatefulconversationApplication.class, args);
	}
}
