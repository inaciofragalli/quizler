package engine.webquiz.quiz;

import engine.webquiz.history.QuizCompletion;
import engine.webquiz.history.QuizCompletionRepository;
import engine.webquiz.user.UserData;
import engine.webquiz.user.User;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
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

    public AnswerResponse guess(@NonNull User currentUser, Long quizId, AnswerRequest req) {
        if (completionRepository.existsByUser_IdAndQuiz_Id(currentUser.getId(), quizId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already completed!");
        }

        Quiz quiz = quizRepository.findById(quizId)
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

        boolean correct =
                quiz.getAnswer().size() == answer.size()
                        && new HashSet<>(quiz.getAnswer())
                        .equals(new HashSet<>(answer));

        if (!correct) {
            return failed;
        }

        QuizCompletion completion = new QuizCompletion();
        completion.setUser(currentUser);
        completion.setQuiz(quiz);

        try {
            completionRepository.saveAndFlush(completion);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Quiz already completed",
                    exception
            );
        }

        return success;
    }

    public void deleteQuiz(@AuthenticationPrincipal @NonNull UserData currentUser, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(QuizNotFoundException::new);

        if (!(quiz.getAuthor().getId().equals(currentUser.getUser().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your quiz!");
        }
        completionRepository.deleteByQuiz(quiz);
        quizRepository.delete(quiz);
    }
}