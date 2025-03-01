package hexlet.code.demo.dto.TaskStatusDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public final class TaskStatusUpdateDTO {
    @NotNull
    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> name;

    @NotNull
    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> slug;
}
