package pl.dayfit.dayguard.Services.Auth.Jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.Auth.UserDetailsImplementation;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Services.Cache.UserCacheService;

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
    public UserDetailsImplementation loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = cacheService.findByEmailOrUsername(identifier);
        return new UserDetailsImplementation(user);
    }
}
