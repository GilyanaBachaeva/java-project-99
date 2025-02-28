package hexlet.code.demo.config;

import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final String ADMIN_EMAIL = "hexlet@example.com";
    private static final String ADMIN_PASSWORD = "qwerty";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncodersConfig encodersConfig;

    @Bean
    CommandLineRunner initialAdmin() {
        return args -> {
            var foundUser = userRepository.findByEmail(ADMIN_EMAIL);
            if (!foundUser.isPresent()) {
                User admin = new User();
                admin.setEmail(ADMIN_EMAIL);
                admin.setPassword(encodersConfig.passwordEncoder().encode(ADMIN_PASSWORD));
                userRepository.save(admin);
            }
        };
    }
}
