package hexlet.code.demo.dto.TaskDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskQueryParamsDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
}
