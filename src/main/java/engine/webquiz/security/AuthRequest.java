package engine.webquiz.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank
        @Size(min = 3)
        String username,

        @NotBlank
        String password
) {
}
