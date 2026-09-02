package engine.webquiz.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank
        @Size(min = 3)
        String username,

        @NotBlank
        @Size(min = 5)
        String password
) {
}
