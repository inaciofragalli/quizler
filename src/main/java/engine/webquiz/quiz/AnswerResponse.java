package engine.webquiz.quiz;

public record AnswerResponse(
        Boolean success,
        String feedback
) {
}