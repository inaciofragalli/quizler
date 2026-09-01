package engine.webquiz.security;

import engine.webquiz.user.User;
import engine.webquiz.user.UserRepository;
import engine.webquiz.user.UserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder encoder;
    private final UserRepository repo;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.encoder = encoder;
        this.repo = repo;
    }

    @Override
    public UserData loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + email));

        return new UserData(user);
    }

    public void registerUser(UserRequest req) {
        if (repo.findByEmail(req.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User newUser = new User();
        newUser.setEmail(req.email());
        newUser.setPassword(encoder.encode(req.password()));

        repo.save(newUser);
    }
}
