package engine.webquiz.quiz;

import engine.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTitle(String title);

    List<Quiz> findByText(String text);

    Page<Quiz> findByAuthor(User author, Pageable pageable);
}
