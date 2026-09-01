package engine.webquiz.history;

import engine.webquiz.quiz.Quiz;
import engine.webquiz.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizCompletionRepository extends JpaRepository<QuizCompletion, Long> {
    @Query("SELECT q FROM QuizCompletion q WHERE q.user = :user")
    Page<QuizCompletion> findQuizCompletions(User user, Pageable pageable);

    @Transactional
    void deleteByQuiz(Quiz quiz);
}
