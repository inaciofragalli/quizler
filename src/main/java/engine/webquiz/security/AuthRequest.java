package engine.webquiz.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank
        @Email(regexp = ".+@.+\\..+")
        String email,

        @NotBlank
        String password
) {
}
