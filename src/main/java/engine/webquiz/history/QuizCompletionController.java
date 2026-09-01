package engine.webquiz.history;

import engine.webquiz.user.UserData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizCompletionController {
    private final QuizCompletionService completionService;

    public QuizCompletionController(QuizCompletionService service) {
        this.completionService = service;
    }

    @GetMapping("/completed")
    public ResponseEntity<Page<QuizCompletionResponse>> getCompletions(@RequestParam(defaultValue = "0") int page, @AuthenticationPrincipal UserData currentUser) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("completedAt").descending());
        Page<QuizCompletionResponse> completions = completionService.getCompletions(currentUser.getUser(), pageable);

        return ResponseEntity.ok(completions);
    }
}
