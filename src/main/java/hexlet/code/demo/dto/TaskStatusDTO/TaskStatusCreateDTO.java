package hexlet.code.demo.dto.TaskStatusDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class TaskStatusCreateDTO {
    @NotNull
    @NotBlank
    @Size(min = 1)
    private String name;

    @NotNull
    @NotBlank
    @Size(min = 1)
    private String slug;
}
