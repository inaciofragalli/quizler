package engine.webquiz.quiz;

import engine.webquiz.security.UserData;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final QuizService service;

    public QuizController(QuizService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<QuizResponse>> getQuizzes(@PageableDefault(size = 10) Pageable pageable) {
        Page<QuizResponse> res = service.getQuizzes(pageable);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable("id") Long id) {
        QuizResponse res = service.getQuizById(id);

        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<QuizCreationResponse> createQuiz(@AuthenticationPrincipal UserData currentUser, @Valid @RequestBody QuizRequest req) {
        QuizResponse savedQuiz = service.createQuiz(currentUser, req);
        QuizCreationResponse res = new QuizCreationResponse(
                savedQuiz.id(),
                savedQuiz.title(),
                savedQuiz.text(),
                savedQuiz.options()
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/solve")
    public ResponseEntity<AnswerResponse> guess(@AuthenticationPrincipal UserData currentUser, @PathVariable Long id, @Valid @RequestBody AnswerRequest req) {
        AnswerResponse res = service.guess(currentUser.getUser(), id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@AuthenticationPrincipal UserData currentUser, @PathVariable("id") Long id) {
        service.deleteQuiz(currentUser, id);

        return ResponseEntity.noContent().build();
    }
}