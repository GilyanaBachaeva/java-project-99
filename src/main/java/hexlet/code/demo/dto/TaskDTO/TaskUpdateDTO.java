package hexlet.code.demo.dto.TaskDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public final class TaskUpdateDTO {
    @NotNull
    @NotBlank
    @Size(min = 1)
    private JsonNullable<String> title;

    private JsonNullable<Integer> index;

    private JsonNullable<String> content;

    @NotNull
    @NotBlank
    private JsonNullable<String> status;

    private JsonNullable<Set<Long>> taskLabelIds;

    private JsonNullable<Long> assigneeId;
}
