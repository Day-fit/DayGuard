package pl.dayfit.dayguard.Services.Auth.Jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.Auth.UserDetailsImplementation;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user with given user has been found"));

        return new UserDetailsImplementation(user);
    }
}
