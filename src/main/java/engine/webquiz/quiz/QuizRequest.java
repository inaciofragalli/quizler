package engine.webquiz.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizRequest(
        @NotBlank(message = "Title cannot be empty")
        String title,

        @NotBlank(message = "Text cannot be empty")
        String text,

        @NotNull
        @Size(min = 2, message = "A quiz must have at least two options")
        List<String> options,

        List<Integer> answer
) {
    public QuizRequest {
        if (answer == null) {
            answer = List.of();
        }
    }
}