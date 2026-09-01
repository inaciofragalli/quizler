package engine.webquiz.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank
        @Email(regexp = ".+@.+\\..+")
        String email,

        @NotBlank
        @Size(min = 5)
        String password
) {
}
