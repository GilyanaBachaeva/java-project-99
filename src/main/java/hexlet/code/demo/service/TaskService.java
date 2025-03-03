package hexlet.code.demo.service;

import hexlet.code.demo.dto.TaskDTO.TaskCreateDTO;
import hexlet.code.demo.dto.TaskDTO.TaskDTO;
import hexlet.code.demo.dto.TaskDTO.TaskQueryParamsDTO;
import hexlet.code.demo.dto.TaskDTO.TaskUpdateDTO;
import hexlet.code.demo.exception.ResourceNotFoundException;
import hexlet.code.demo.mapper.TaskMapper;
import hexlet.code.demo.model.Task;
import hexlet.code.demo.repository.TaskRepository;
import hexlet.code.demo.specification.TaskSpecificarion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public final class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskSpecificarion specBuilder;

    public List<TaskDTO> getAll(TaskQueryParamsDTO taskQueryParamsDTO) {
        var spec = specBuilder.build(taskQueryParamsDTO);
        return taskRepository.findAll(spec)
                .stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " not found."));
        TaskDTO dto = taskMapper.map(task);
        return dto;
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        Task task = taskMapper.map(taskData);
        taskRepository.save(task);
        TaskDTO dto = taskMapper.map(task);
        return dto;
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        Task model = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " not found."));
        taskMapper.update(taskData, model);
        taskRepository.save(model);
        TaskDTO dto = taskMapper.map(model);
        return dto;
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
