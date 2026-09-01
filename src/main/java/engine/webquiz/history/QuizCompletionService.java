package engine.history;

import engine.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuizCompletionService {
    private final QuizCompletionRepository completionRepository;

    public QuizCompletionService(QuizCompletionRepository repo) {
        this.completionRepository = repo;
    }

    public Page<QuizCompletionResponse> getCompletions(User currentUser, Pageable pageable) {
        Page<QuizCompletion> completions = completionRepository.findQuizCompletions(currentUser, pageable);

        return completions.map(completion -> new QuizCompletionResponse(
                completion.getQuiz().getId(), // Grabbing the Quiz ID here!
                completion.getCompletedAt()
        ));
    }
}
