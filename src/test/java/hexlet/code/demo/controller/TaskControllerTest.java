package hexlet.code.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.demo.config.DataInitializer;
import hexlet.code.demo.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.demo.dto.TaskDTO.TaskDTO;
import hexlet.code.demo.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.demo.mapper.TaskMapper;
import hexlet.code.demo.model.Task;
import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.TaskRepository;
import hexlet.code.demo.repository.TaskStatusRepository;
import hexlet.code.demo.repository.UserRepository;
import hexlet.code.demo.util.ModelGenerator;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    private TaskStatus testTaskStatus;

    private Task testTask;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();

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
    void testIndexTasks() throws Exception {
        taskRepository.save(testTask);
        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        var amountOfTaskFromResponse = objectMapper.readValue(body, TaskDTO[].class).length;
        var amountOfTaskFromDb = taskRepository.count();
        assertThat(amountOfTaskFromDb == amountOfTaskFromResponse);
    }

    @Test
    public void testCreateTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle(testTask.getName());
        taskCreateDTO
                .setStatus(taskStatusRepository.findBySlug(testTaskStatus.getSlug())
                        .get()
                        .getSlug());
        var createTaskRequest = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDTO));
        String taskDtoResponse = mockMvc.perform(createTaskRequest.with(token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThatJson(taskDtoResponse).and(
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("status").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testShowTask() throws Exception {
        taskRepository.save(testTask);
        var getTaskRequest = get("/api/tasks/{id}", testTask.getId());
        var result = mockMvc.perform(getTaskRequest.with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug())
        );
    }

    @Test
    public void testUpdateTask() throws Exception {
        taskRepository.save(testTask);
        userRepository.save(testUser);
        Long createdUserId = userRepository.findByEmail(testUser.getEmail()).get().getId();
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle(JsonNullable.of(testTask.getName() + " updated"));
        taskUpdateDTO.setContent(JsonNullable.of("there was no content, now its there"));
        taskUpdateDTO.setAssigneeId(JsonNullable.of(createdUserId));
        var updateTaskRequest = put("/api/tasks/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdateDTO));
        String taskDTOAsStringFromResponse = mockMvc.perform(updateTaskRequest.with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThatJson(taskDTOAsStringFromResponse).and(
                v -> v.node("id").isEqualTo(testTask.getId()),
                v -> v.node("assigneeId").isEqualTo(createdUserId),
                v -> v.node("title").isEqualTo(taskUpdateDTO.getTitle()),
                v -> v.node("content").isEqualTo(taskUpdateDTO.getContent()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getSlug())
        );
    }

    @Test
    public void testDeleteTask() throws Exception {
        taskRepository.save(testTask);
        Long createdTaskId = taskRepository.findById(testTask.getId())
                .get()
                .getId();
        var deleteTaskRequest = delete("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(deleteTaskRequest.with(token)).andExpect(status().isNoContent());
    }
}
