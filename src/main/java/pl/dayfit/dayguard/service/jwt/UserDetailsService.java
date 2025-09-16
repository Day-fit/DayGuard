package pl.dayfit.dayguard.service.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.auth.UserCredentials;
import pl.dayfit.dayguard.entity.User;
import pl.dayfit.dayguard.service.cache.UserCacheService;

@Component
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserCacheService cacheService;

    /**
     * Loads user by email or username.
     *
     * @param identifier email or username
     * @return UserDetailsImplementation of the loaded user
     * @throws UsernameNotFoundException if no user with the given identifier exists
     */
    @Override
    public UserCredentials loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = cacheService.findByEmailOrUsername(identifier);
        return new UserCredentials(user);
    }
}
