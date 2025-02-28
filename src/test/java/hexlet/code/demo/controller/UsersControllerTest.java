package hexlet.code.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.demo.dto.UserDTO;
import hexlet.code.demo.dto.UserUpdateDTO;
import hexlet.code.demo.mapper.UserMapper;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.UserRepository;
import hexlet.code.demo.util.ModelGenerator;
import hexlet.code.demo.util.UserUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    private ModelGenerator modelGenerator;

    @Autowired
    private UserUtils userUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
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
        var amountOfUsersInDb = userRepository.count();
        var amountOfUsersInResponse = objectMapper.readValue(body, UserDTO[].class).length;
        assertThat(amountOfUsersInDb == amountOfUsersInResponse);
    }

    @Test
    public void testCreateUser() throws Exception {
        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(request).andExpect(status().isCreated());
        var user = userRepository.findByEmail(testUser.getEmail())
                .orElseThrow(() -> new AssertionError("User not found after creation"));
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
        mockMvc.perform(createUserRequest);
        var createdUser = userRepository.findByEmail(testUser.getEmail())
                .orElseThrow(() -> new AssertionError("User not found after creation"));
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
                .content(objectMapper.writeValueAsString(dto));
        mockMvc.perform(request.with(token)).andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUser() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of("someguy14@gmail.com"));
        var createUserRequest = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(createUserRequest);
        var createdUser = userRepository.findByEmail(testUser.getEmail())
                .orElseThrow(() -> new AssertionError("User not found after creation"));
        var updateUserRequest = put("/api/users/{id}", createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO));
        mockMvc.perform(updateUserRequest.with(token)).andExpect(status().isOk());
        var updatedUser = userRepository.findById(createdUser.getId())
                .orElseThrow(() -> new AssertionError("User not found after update"));
        assertThatJson(updatedUser).and(
                v -> v.node("id").isEqualTo(createdUser.getId()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(updateDTO.getEmail())
        );
    }

    @Test
    public void testDeleteUserCorrectToken() throws Exception {
        var createUserRequest = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(createUserRequest);
        var createdUser = userRepository.findByEmail(testUser.getEmail())
                .orElseThrow(() -> new AssertionError("User not found after creation"));
        var deleteUserRequest = delete("/api/users/{id}", createdUser.getId());
        mockMvc.perform(deleteUserRequest.with(token)).andExpect(status().isNoContent());
        assertThat(userRepository.existsById(createdUser.getId())).isFalse();
    }

    @Test
    public void testDeleteUserIncorrectToken() throws Exception {
        var createUserRequest = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser));
        mockMvc.perform(createUserRequest);
        var createdUser = userRepository.findByEmail(testUser.getEmail())
                .orElseThrow(() -> new AssertionError("User not found after creation"));
        var deleteUserRequest = delete("/api/users/{id}", createdUser.getId());
        var incorrectToken = jwt().jwt(builder -> builder.subject("lumpa14@mail.ru"));
        mockMvc.perform(deleteUserRequest.with(incorrectToken)).andExpect(status().isForbidden());
    }
}
