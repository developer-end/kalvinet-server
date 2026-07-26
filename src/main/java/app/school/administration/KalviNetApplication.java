package app.school.administration;

import app.school.administration.common.domain.model.JWTProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(JWTProperties.class)
public class KalviNetApplication {

    public static void main(String[] args) {
        SpringApplication.run(KalviNetApplication.class, args);
    }

}
