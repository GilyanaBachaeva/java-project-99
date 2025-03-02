package hexlet.code.demo.dto.LabelDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LabelCreateDTO {
    @NotBlank
    @NotNull
    @Size(min = 3, max = 1000)
    private String name;
}
