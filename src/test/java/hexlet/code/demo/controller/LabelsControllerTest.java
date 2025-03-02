package hexlet.code.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.demo.config.DataInitializer;
import hexlet.code.demo.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.demo.dto.LabelDTO.LabelDTO;
import hexlet.code.demo.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.demo.mapper.LabelMapper;
import hexlet.code.demo.model.Label;
import hexlet.code.demo.model.Task;
import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.model.User;
import hexlet.code.demo.repository.LabelRepository;
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
public class LabelsControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private LabelMapper labelMapper;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private Task testTask;

    private TaskStatus testTaskStatus;

    private Label testLabel;

    private User testUser;

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
        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
    }

    @Test
    void testIndexLabels() throws Exception {
        labelRepository.save(testLabel);
        var result = mockMvc.perform(get("/api/labels").with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        List<LabelDTO> labelDTOs = objectMapper.readValue(body, new TypeReference<>() { });
        List<Label> actualLabels = labelDTOs.stream().map(labelMapper::map).toList();
        List<Label> expectedLabels = labelRepository.findAll();
        assertThat(actualLabels.containsAll(expectedLabels));
    }

    @Test
    public void testCreateLabel() throws Exception {
        LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName(testLabel.getName());
        var createLabelRequest = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelCreateDTO));
        String labelDtoResponse = mockMvc.perform(createLabelRequest.with(token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThatJson(labelDtoResponse).and(
                v -> v.node("name").isEqualTo(testLabel.getName())
        );
    }

    @Test
    public void testShowLabel() throws Exception {
        labelRepository.save(testLabel);
        var getLabelRequest = get("/api/labels/{id}", testLabel.getId());
        var result = mockMvc.perform(getLabelRequest.with(token))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel.getName())
        );
    }

    @Test
    public void testUpdateLabel() throws Exception {
        labelRepository.save(testLabel);
        //Long createdLabelId = labelRepository.findByName(testLabel.getName()).get().getId();
        LabelUpdateDTO labelUpdateDTO = new LabelUpdateDTO();
        labelUpdateDTO.setName(JsonNullable.of(testLabel.getName() + " updated"));
        var updateLabelRequest = put("/api/labels/{id}", testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(labelUpdateDTO));
        String labelDTOAsStringFromResponse = mockMvc.perform(updateLabelRequest.with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThatJson(labelDTOAsStringFromResponse).and(
                v -> v.node("id").isEqualTo(testLabel.getId()),
                v -> v.node("name").isEqualTo(labelUpdateDTO.getName())
        );
    }

    @Test
    public void testDeleteLabelWithoutConnectedTask() throws Exception {
        labelRepository.save(testLabel);
        var deleteLabelRequest = delete("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(deleteLabelRequest.with(token)).andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteLabelWithConnectedTask() throws Exception {
        taskStatusRepository.save(testTaskStatus);
        labelRepository.save(testLabel);
        Set<Long> labelIdsFromRepo = labelRepository.findAll()
                .stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
        testTask.setLabels(labelRepository.findByIdIn(labelIdsFromRepo));
        taskRepository.save(testTask);
        var deleteLabelRequest = delete("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(deleteLabelRequest.with(token)).andExpect(status().isUnprocessableEntity());
    }
}
