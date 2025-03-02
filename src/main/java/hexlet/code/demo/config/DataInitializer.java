package hexlet.code.demo.config;

import hexlet.code.demo.model.Label;
import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.LabelRepository;
import hexlet.code.demo.repository.TaskStatusRepository;
import hexlet.code.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
@Component
@RequiredArgsConstructor
public final class DataInitializer {

    private static final String ADMIN_EMAIL = "hexlet@example.com";
    private static final String ADMIN_PASSWORD = "qwerty";
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final EncodersConfig encodersConfig;

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

    public static final List<String> DEFAULT_LABEL_NAMES =
            new LinkedList<>(
                    List.of(
                            "feature",
                            "bug"
                    )
            );

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
            var taskStatusEntries = DEFAULT_TASK_STATUSES_SLUGS_AND_NAMES_MAP.entrySet();
            for (var taskStatusSlugAndName : taskStatusEntries) {
                if (taskStatusRepository.findBySlug(taskStatusSlugAndName.getKey()).isEmpty()) {
                    TaskStatus taskStatus = new TaskStatus(taskStatusSlugAndName.getKey(),
                            taskStatusSlugAndName.getValue());
                    taskStatusRepository.save(taskStatus);
                }
            }
            for (int i = 0; i < DEFAULT_LABEL_NAMES.size(); i++) {
                if (labelRepository.findByName(DEFAULT_LABEL_NAMES.get(i)).isEmpty()) {
                    Label label = new Label(DEFAULT_LABEL_NAMES.get(i));
                    labelRepository.save(label);
                }
            }
        };
    }
}
