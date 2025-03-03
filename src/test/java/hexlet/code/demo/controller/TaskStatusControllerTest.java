package hexlet.code.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.demo.config.DataInitializer;
import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusDTO;
import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusUpdateDTO;
import hexlet.code.demo.mapper.TaskStatusMapper;
import hexlet.code.demo.model.Task;
import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.LabelRepository;
import hexlet.code.demo.repository.TaskRepository;
import hexlet.code.demo.repository.TaskStatusRepository;
import hexlet.code.demo.repository.UserRepository;
import hexlet.code.demo.util.ModelGenerator;
import hexlet.code.demo.util.UserUtils;
import org.assertj.core.api.Assertions;
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
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class TaskStatusControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserUtils userUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    private TaskStatus testTaskStatus;

    private Task testTask;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        for (var entry : DataInitializer.DEFAULT_TASK_STATUSES_SLUGS_AND_NAMES_MAP.entrySet()) {
            TaskStatus taskStatus = new TaskStatus(entry.getKey(), entry.getValue());
            taskStatusRepository.save(taskStatus);
        }
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
    }

    @Test
    void testIndexTaskStatus() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var result = mockMvc.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<TaskStatus> statusesInDb = taskStatusRepository.findAll();
        List<TaskStatusDTO> dtoFromResponse = objectMapper.readValue(body, new TypeReference<>() { });
        List<TaskStatus> modelFromResponse = dtoFromResponse.stream()
                .map(taskStatusMapper::map)
                .toList();
        Assertions.assertThat(statusesInDb).containsExactlyInAnyOrderElementsOf(modelFromResponse);
    }

    @Test
    void testShowTaskStatus() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var getTaskStatusRequest = get("/api/task_statuses/{id}", testTaskStatus.getId());
        var result = mockMvc.perform(getTaskStatusRequest.with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    void testCreateTaskStatus() throws Exception {
        var createTaskStatusRequest = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskStatus));
        mockMvc.perform(createTaskStatusRequest.with(token)).andExpect(status().isCreated());
        var createdTaskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get();
        assertThat(createdTaskStatus).isNotNull();
        assertThat(createdTaskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(createdTaskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @Test
    void testCreateTaskStatusByUnauthorisedUser() throws Exception {
        var createTaskStatusRequest = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskStatus));
        mockMvc.perform(createTaskStatusRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateExistingSlugTaskStatus() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var createTaskStatusRequest = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskStatus));
        assertThat(taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get()).isNotNull();
        mockMvc.perform(createTaskStatusRequest.with(token)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateTaskStatus() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        String newTaskName = "New name for slug " + testTaskStatus.getSlug();
        TaskStatusUpdateDTO taskStatusUpdateDTO = new TaskStatusUpdateDTO();
        taskStatusUpdateDTO.setName(JsonNullable.of(newTaskName));
        var updateRequest = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskStatusUpdateDTO));
        mockMvc.perform(updateRequest.with(token)).andExpect(status().isOk());
        var updatedTaskStatus = taskStatusRepository.findBySlug(testTaskStatus.getSlug()).get();
        assertThatJson(updatedTaskStatus).and(
                v -> v.node("name").isEqualTo(taskStatusUpdateDTO.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    void testDeleteTaskStatus() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var deleteRequest = delete("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(deleteRequest.with(token)).andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteTaskStatusConnectedWithTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        testTask.setTaskStatus(testTaskStatus);
        taskRepository.save(testTask);
        var deleteTaskStatusRequest = delete("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(deleteTaskStatusRequest.with(token)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testDeleteIncorrectTaskStatus() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        var deleteRequest = delete("/api/task_statuses/9999");
        mockMvc.perform(deleteRequest.with(token)).andExpect(status().isNoContent());
    }
}
