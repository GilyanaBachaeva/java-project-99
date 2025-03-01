package hexlet.code.demo.mapper;

import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusCreateDTO;
import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusDTO;
import hexlet.code.demo.dto.TaskStatusDTO.TaskStatusUpdateDTO;
import hexlet.code.demo.model.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper (
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)

public abstract class TaskStatusMapper {
    public abstract TaskStatus map(TaskStatusCreateDTO dto);
    public abstract TaskStatusDTO map(TaskStatus model);
    public abstract TaskStatus map(TaskStatusDTO model);
    public abstract void update(TaskStatusUpdateDTO dto, @MappingTarget TaskStatus model);
}
