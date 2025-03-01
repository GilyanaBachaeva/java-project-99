package hexlet.code.demo.config;

import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.TaskStatusRepository;
import hexlet.code.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DataInitializer {

    private static final String ADMIN_EMAIL = "hexlet@example.com";
    private static final String ADMIN_PASSWORD = "qwerty";

    public static final Map<String, String> DEFAULT_TASK_STATUSES_SLUGS_AND_NAMES_MAP =
            new LinkedHashMap<>(
                    Map.of(
                            "draft",
                            "Draft",
                            "to_review",
                            "ToReview",
                            "to_be_fixed",
                            "ToBeFixed",
                            "to_publish",
                            "ToPublish",
                            "published",
                            "Published"
                    ));

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

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
            var entries = DEFAULT_TASK_STATUSES_SLUGS_AND_NAMES_MAP.entrySet();
            for (var entry : entries) {
                if (taskStatusRepository.findBySlug(entry.getKey()).isEmpty()) {
                    TaskStatus taskStatus = new TaskStatus(entry.getKey(), entry.getValue());
                    taskStatusRepository.save(taskStatus);
                }
            }
        };
    }
}
