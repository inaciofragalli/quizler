package engine.webquiz.user;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final PasswordEncoder encoder;
    private final UserRepository repo;

    @Override
    public UserData loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));

        return new UserData(user);
    }

    public void registerUser(UserRequest req) {
        if (repo.findByUsername(req.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        User newUser = new User();
        newUser.setUsername(req.username());
        newUser.setPassword(encoder.encode(req.password()));

        repo.save(newUser);
    }
}
