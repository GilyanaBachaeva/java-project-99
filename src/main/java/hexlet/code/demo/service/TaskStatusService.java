package hexlet.code.demo.service;

import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusDTO;
import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusUpdateDTO;
import hexlet.code.demo.exception.ResourceNotFoundException;
import hexlet.code.demo.mapper.TaskStatusMapper;
import hexlet.code.demo.model.TaskStatus;
import hexlet.code.demo.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class TaskStatusService {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public List<TaskStatusDTO> getAll() {
        List<TaskStatus> tasks = taskStatusRepository.findAll();
        List<TaskStatusDTO> result = tasks.stream()
                .map(taskStatusMapper::map)
                .toList();
        return result;
    }

    public TaskStatusDTO getById(Long id) {
        TaskStatus model = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id: " + id + " not found"));
        TaskStatusDTO dto = taskStatusMapper.map(model);
        return dto;
    }

    public TaskStatusDTO create(TaskStatusCreateDTO taskStatusData) {
        TaskStatus model = taskStatusMapper.map(taskStatusData);
        taskStatusRepository.save(model);
        TaskStatusDTO dto = taskStatusMapper.map(model);
        return dto;
        //here if save 2 same slug - 422 should be thrown. Point for check
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id) {
        TaskStatus model = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id: " + id + " not found"));
        taskStatusMapper.update(taskStatusData, model);
        taskStatusRepository.save(model);
        TaskStatusDTO dto = taskStatusMapper.map(model);
        return dto;
    }

    public void delete(Long id) {
        taskStatusRepository.deleteById(id);
    }
}
