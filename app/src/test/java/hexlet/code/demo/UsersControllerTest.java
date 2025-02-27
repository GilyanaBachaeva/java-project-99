package hexlet.code.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.demo.dto.UserUpdateDTO;
import hexlet.code.demo.mapper.UserMapper;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.UserRepository;
import hexlet.code.demo.util.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class UsersControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserUtils userUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
    }

    @Test
    public void testWelcome() throws Exception {
        var result = mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThat(body.contains("Welcome"));
    }

    @Test
    public void testIndexUsers() throws Exception {
        var result = mockMvc.perform(get("/api/users").with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreateUser() throws Exception {
        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        var result = mockMvc.perform(request.with(token))
                .andExpect(status().isCreated());
        var user = userRepository.findByEmail(testUser.getEmail()).get();
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getPassword()).isNotEqualTo(testUser.getPassword());
    }

    @Test
    public void testShowUser() throws Exception {
        var createUserRequest = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(createUserRequest.with(token));
        var createdUser = userRepository.findByEmail(testUser.getEmail()).get();
        var getUserRequest = get("/api/users/{id}", createdUser.getId());
        var result = mockMvc.perform(getUserRequest.with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail())
        );
    }

    @Test
    public void testCreateUserWithNotCorrectEmail() throws Exception {
        var dto = userMapper.map(testUser);
        dto.setEmail("qwerty");
        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(dto));
        mockMvc.perform(request.with(token)).andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUser() throws Exception {
        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of("someguy14@gmail.com"));
        var createUserRequest = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(createUserRequest.with(token));
        var createdUser = userRepository.findByEmail(testUser.getEmail()).get();
        var request = put("/api/users/{id}", createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(dto));
        mockMvc.perform(request.with(token)).andExpect(status().isOk());
        var updatedUser = userRepository.findById(createdUser.getId()).get();
        assertThatJson(updatedUser).and(
                v -> v.node("id").isEqualTo(createdUser.getId()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(dto.getEmail())
        );
    }

    @Test
    public void testDeleteUser() throws Exception {
        var createUserRequest = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(createUserRequest.with(token));
        var createdUser = userRepository.findByEmail(testUser.getEmail()).get();
        var request = delete("/api/users/{id}", createdUser.getId());
        mockMvc.perform(request.with(token)).andExpect(status().isNoContent());
        assertThat(userRepository.existsById(createdUser.getId())).isFalse();
    }
}
