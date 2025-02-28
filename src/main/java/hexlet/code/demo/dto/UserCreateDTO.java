package hexlet.code.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {
    @NotNull
    @Email
    private String email;

    private String firstName;

    private String lastName;

    @NotNull
    @NotBlank
    @Size(min = 3)
    private String password;
}
