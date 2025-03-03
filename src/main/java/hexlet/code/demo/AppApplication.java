package hexlet.code.demo;

import io.github.cdimascio.dotenv.Dotenv;
import io.sentry.Sentry;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }

    private static void testSentry() {
        try {
            throw new Exception("This is a Sentry test.");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
