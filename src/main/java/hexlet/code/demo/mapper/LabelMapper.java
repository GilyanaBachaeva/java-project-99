package hexlet.code.demo.mapper;

import hexlet.code.demo.dto.LabelDTO.LabelCreateDTO;
import hexlet.code.demo.dto.LabelDTO.LabelDTO;
import hexlet.code.demo.dto.LabelDTO.LabelUpdateDTO;
import hexlet.code.demo.model.Label;
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
public abstract class LabelMapper {
    public abstract Label map(LabelCreateDTO createDTO);
    public abstract LabelDTO map(Label model);
    public abstract Label map(LabelDTO dto);
    public abstract void update(LabelUpdateDTO updateDTO, @MappingTarget Label model);
}
