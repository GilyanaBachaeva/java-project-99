package hexlet.code.demo.dto.TaskStatusDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public final class TaskStatusDTO {
    private Long id;
    private String name;
    private String slug;
    private LocalDate createdAt;
}
