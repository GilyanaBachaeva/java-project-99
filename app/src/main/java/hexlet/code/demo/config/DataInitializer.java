package hexlet.code.demo.config;

import hexlet.code.demo.dto.UserDTO;
import hexlet.code.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        UserDTO adminUser = new UserDTO();
        adminUser.setEmail("hexlet@example.com");
        adminUser.setPassword("qwerty");
        userService.createUser(adminUser);
    }
}
