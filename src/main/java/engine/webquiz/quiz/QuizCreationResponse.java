package engine.webquiz.quiz;

import java.util.List;

public record QuizCreationResponse(
        Long id,
        String title,
        String text,
        List<String> options
) {
}
