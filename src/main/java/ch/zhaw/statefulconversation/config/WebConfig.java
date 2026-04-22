package ch.zhaw.statefulconversation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS-Konfiguration fuer das Oblivio-Backend (PROMISE Framework).
 *
 * Erlaubt Cross-Origin-Zugriffe von der Produktions-Domain (oblivio.ch)
 * sowie von lokalen Entwicklungsumgebungen (localhost:8080, localhost:3000).
 * Preflight-Responses werden fuer 1 Stunde gecacht, um die Anzahl
 * OPTIONS-Requests zu reduzieren.
 */
@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "https://oblivio.ch",
                                "https://www.oblivio.ch",
                                "http://localhost:8080",
                                "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}
