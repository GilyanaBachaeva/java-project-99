package hexlet.code.demo.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public final class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate createdAt;
}
