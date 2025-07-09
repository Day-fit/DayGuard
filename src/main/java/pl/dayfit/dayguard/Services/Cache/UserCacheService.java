package pl.dayfit.dayguard.Services.Cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Repositories.UserRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final UserRepository userRepository;

    @Caching(put =
        {
                @CachePut(value = "user", key = "#user.username"),
                @CachePut(value = "user", key = "#user.email"),
                @CachePut(value = "user", key = "#user.id")
        }
    )
    public User save(User user)
    {
        return userRepository.save(user);
    }

    /**
     * Finds user by username or email.
     * @param identifier identifier compared to username and email in a query
     * @return User matching given username or email
     * @throws UsernameNotFoundException if no user exists with a given username or email
     */
    @Cacheable(value = "user", key = "#identifier")
    public User findByEmailOrUsername(String identifier) throws UsernameNotFoundException
    {
        return userRepository.findByEmailOrUsername(identifier)
                .orElseThrow(() -> new BadCredentialsException("Given credentials are incorrect"));
    }

    @Cacheable(value = "user", key = "#username")
    public User findByUsername(String username)
    {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("No user with username " + username + " has been found"));
    }

    @Cacheable(value = "user", key = "#email")
    public User findByEmail(String email)
    {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("No user with username " + email + " has been found"));
    }

    /**
     * Finds user by id.
     * @param id user id
     * @return User with given id
     * @throws NoSuchElementException if no user exists with given, id
     */
    @Cacheable(value = "user", key = "#id")
    @SuppressWarnings("unused")
    public User findById(Long id) throws NoSuchElementException
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no user with given id"));
    }

    @SuppressWarnings("unused")
    @Caching(evict =
            {
                    @CacheEvict(value = "user", key = "#result.id"),
                    @CacheEvict(value = "user", key = "#result.username"),
                    @CacheEvict(value = "user", key = "#result.email")
            }
    )
    public User delete(Long id)
    {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("There is no user with given id"));

        userRepository.delete(user);

        return user;
    }
}
