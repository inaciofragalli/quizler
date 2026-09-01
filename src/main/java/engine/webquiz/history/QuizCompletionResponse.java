package engine.webquiz.history;

import java.time.LocalDateTime;

public record QuizCompletionResponse(
    Long id,
    LocalDateTime completedAt
) {
}
