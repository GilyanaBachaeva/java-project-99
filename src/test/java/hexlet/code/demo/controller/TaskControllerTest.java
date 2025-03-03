package hexlet.code.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.demo.config.DataInitializer;
import hexlet.code.demo.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.demo.dto.TaskDTO.TaskDTO;
import hexlet.code.demo.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.demo.mapper.TaskMapper;
import hexlet.code.demo.model.Label;
import hexlet.code.demo.model.Task;
import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.LabelRepository;
import hexlet.code.demo.repository.TaskRepository;
import hexlet.code.demo.repository.TaskStatusRepository;
import hexlet.code.demo.repository.UserRepository;
import hexlet.code.demo.util.ModelGenerator;
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
import java.util.Set;
import java.util.stream.Collectors;

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
    private LabelRepository labelRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private User testUser;

    private TaskStatus testTaskStatus;

    private Task testTask;

    private Label testLabel;

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
        for (var labelName : DataInitializer.DEFAULT_LABEL_NAMES) {
            Label label = new Label(labelName);
            labelRepository.save(label);
        }
        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
    }

    @Test
    void testIndexTasksWithoutQueryString() throws Exception {
        taskRepository.save(testTask);
        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<TaskDTO> taskFromResponse = objectMapper.readValue(body, new TypeReference<>() { });
        List<Task> tasksFromDb = taskRepository.findAll();
        List<Task> taskModelFromResponse = taskFromResponse.stream()
                .map(taskMapper::map)
                .toList();
        Assertions.assertThat(tasksFromDb).containsExactlyInAnyOrderElementsOf(taskModelFromResponse);
    }

    @Test
    void testIndexTasksWithQueryTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        labelRepository.save(testLabel);
        userRepository.save(testUser);
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testTaskStatus);
        testTask.getLabels().add(testLabel);
        testTask.setName("XxXbrx name");
        taskRepository.save(testTask);
        StringBuilder query = new StringBuilder("?_end=100&_order=ASC&_sort=index&_start=0");
        query.append("&assigneeId=" + testUser.getId());
        query.append("&labelId=" + testLabel.getId());
        query.append("&status=" + testTaskStatus.getSlug());
        query.append("&titleCont=" + "xxx");
        String queryString = query.toString();
        var result = mockMvc.perform(get("/api/tasks" + queryString).with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<TaskDTO> response = objectMapper.readValue(body, new TypeReference<>() { });
        assertThat(response.size() == 1);
        assertThat(response.contains(taskMapper.map(testTask)));
    }


    @Test
    public void testCreateTaskWithoutLabels() throws Exception {
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
    public void testCreateTaskWithLabels() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        userRepository.save(testUser);
        labelRepository.save(testLabel);
        Set<Long> labelIdsFromRepo = labelRepository.findAll()
                .stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle(testTask.getName());
        taskCreateDTO.setContent(testTask.getDescription());
        taskCreateDTO.setStatus(testTaskStatus.getSlug());
        taskCreateDTO
                .setStatus(taskStatusRepository.findBySlug(testTaskStatus.getSlug())
                        .get()
                        .getSlug());
        taskCreateDTO
                .setTaskLabelIds(labelIdsFromRepo);
        taskCreateDTO.setAssigneeId(testUser.getId());
        var createTaskRequest = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskCreateDTO));
        var responseBody = mockMvc.perform(createTaskRequest.with(token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        TaskDTO taskDTO = objectMapper.readValue(responseBody, new TypeReference<>() { });
        assertThat(taskDTO.getAssigneeId().equals(testUser.getId()));
        assertThat(taskDTO.getTitle().equals(testTask.getName()));
        assertThat(taskDTO.getContent().equals(testTask.getDescription()));
        assertThat(taskDTO.getTaskLabelIds().containsAll(labelIdsFromRepo));
        assertThat(taskDTO.getStatus().equals(testTaskStatus.getSlug()));
    }

    @Test
    public void testShowTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        userRepository.save(testUser);
        labelRepository.save(testLabel);
        Set<Long> labelIdsFromRepo = labelRepository.findAll()
                .stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
        testTask.setLabels(labelRepository.findByIdIn(labelIdsFromRepo));
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        taskRepository.save(testTask);
        var getTaskRequest = get("/api/tasks/{id}", testTask.getId());
        var result = mockMvc.perform(getTaskRequest.with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        TaskDTO taskDTO = objectMapper.readValue(body, new TypeReference<>() { });
        assertThat(taskDTO.getIndex().equals(testTask.getIndex()));
        assertThat(taskDTO.getAssigneeId().equals(testUser.getId()));
        assertThat(taskDTO.getTitle().equals(testTask.getName()));
        assertThat(taskDTO.getContent().equals(testTask.getDescription()));
        assertThat(taskDTO.getTaskLabelIds().containsAll(labelIdsFromRepo));
        assertThat(taskDTO.getStatus().equals(testTaskStatus.getSlug()));
    }

    @Test
    public void testUpdateTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        userRepository.save(testUser);
        labelRepository.save(testLabel);
        taskRepository.save(testTask);
        Set<Long> labelIdsFromRepo = labelRepository.findAll()
                .stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle(JsonNullable.of(testTask.getName() + " updated"));
        taskUpdateDTO.setIndex(JsonNullable.of(testTask.getIndex() + 10));
        taskUpdateDTO.setContent(JsonNullable.of("New content for description section"));
        taskUpdateDTO.setStatus(JsonNullable.of(testTaskStatus.getSlug()));
        taskUpdateDTO.setTaskLabelIds(JsonNullable.of(labelIdsFromRepo));
        taskUpdateDTO.setAssigneeId(JsonNullable.of(testUser.getId()));
        var updateTaskRequest = put("/api/tasks/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdateDTO));
        String taskDTOAsStringFromResponse = mockMvc.perform(updateTaskRequest.with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        TaskDTO taskDTO = objectMapper.readValue(taskDTOAsStringFromResponse, new TypeReference<>() { });
        assertThat(taskDTO.getIndex().equals(testTask.getIndex() + 10));
        assertThat(taskDTO.getAssigneeId().equals(testUser.getId()));
        assertThat(taskDTO.getTitle().equals(testTask.getName()));
        assertThat(taskDTO.getContent().equals(testTask.getDescription()));
        assertThat(taskDTO.getTaskLabelIds().containsAll(labelIdsFromRepo));
        assertThat(taskDTO.getStatus().equals(testTaskStatus.getSlug()));
    }

    @Test
    public void testDeleteTask() throws Exception {
        taskRepository.save(testTask);
        var deleteTaskRequest = delete("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(deleteTaskRequest.with(token)).andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteIncorrectTask() throws Exception {
        taskRepository.save(testTask);
        var deleteTaskRequest = delete("/api/tasks/999");
        mockMvc.perform(deleteTaskRequest.with(token)).andExpect(status().isNoContent());
    }
}
