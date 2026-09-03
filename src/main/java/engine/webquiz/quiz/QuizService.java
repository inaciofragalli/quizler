package engine.webquiz.quiz;

import engine.webquiz.history.QuizCompletion;
import engine.webquiz.history.QuizCompletionRepository;
import engine.webquiz.user.UserData;
import engine.webquiz.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizCompletionRepository completionRepository;

    public QuizService(QuizRepository repo, QuizCompletionRepository compRepo) {
        this.quizRepository = repo;
        this.completionRepository = compRepo;
    }

    public Page<QuizResponse> getQuizzes(Pageable pageable) {
        Page<Quiz> quizzes = quizRepository.findAll(pageable);

        return quizzes.map(quiz -> new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getText(),
                quiz.getOptions(),
                quiz.getAuthor().getUsername()
        ));
    }

    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getText(),
                quiz.getOptions(),
                quiz.getAuthor().getUsername()
        );
    }

    public QuizResponse createQuiz(@AuthenticationPrincipal UserData currentUser, QuizRequest req) {
        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(req.title());
        newQuiz.setText(req.text());
        newQuiz.setOptions(req.options());
        newQuiz.setAnswer(req.answer());
        newQuiz.setAuthor(currentUser.getUser());

        quizRepository.save(newQuiz);

        return new QuizResponse(
                newQuiz.getId(),
                newQuiz.getTitle(),
                newQuiz.getText(),
                newQuiz.getOptions(),
                newQuiz.getAuthor().getUsername()
        );
    }

    public AnswerResponse guess(User currentUser, Long id, AnswerRequest req) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(QuizNotFoundException::new);

        List<Integer> answer = Optional.ofNullable(req.answer())
                .orElse(Collections.emptyList());

        AnswerResponse success = new AnswerResponse(
                true,
                "Congratulations, you're right!"
        );

        AnswerResponse failed = new AnswerResponse(
                false,
                "Wrong answer! Please try again."
        );

        if (answer.isEmpty() && quiz.getAnswer().isEmpty()) {
            QuizCompletion newCompletion = new QuizCompletion();
            newCompletion.setUser(currentUser);
            newCompletion.setQuiz(quiz);

            completionRepository.save(newCompletion);

            return success;

        } else if (quiz.getAnswer().size() == answer.size()
                && new HashSet<>(quiz.getAnswer()).containsAll(answer)) {
            QuizCompletion newCompletion = new QuizCompletion();
            newCompletion.setUser(currentUser);
            newCompletion.setQuiz(quiz);

            completionRepository.save(newCompletion);
            return success;
        }

        return failed;
    }

    public void deleteQuiz(@AuthenticationPrincipal UserData currentUser, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(QuizNotFoundException::new);

        if (!(quiz.getAuthor().getId().equals(currentUser.getUser().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your quiz!");
        }
        completionRepository.deleteByQuiz(quiz);
        quizRepository.delete(quiz);
    }
}