package hexlet.code.demo.dto.LabelDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public final class LabelDTO {
    private Long id;
    private String name;
    private LocalDate createdAt;
}
