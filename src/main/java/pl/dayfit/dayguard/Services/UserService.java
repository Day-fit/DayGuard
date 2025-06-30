package pl.dayfit.dayguard.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.DTOs.Auth.RegisterDTO;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Exceptions.UserAlreadyExistException;
import pl.dayfit.dayguard.Repositories.UserRepository;
import pl.dayfit.dayguard.Services.Cache.UserCacheService;

import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterDTO dto)
    {
        String username = dto.getUsername();
        String email = dto.getEmail();

        if (userRepository.existsByEmailOrUsername(email, username))
        {
            throw new UserAlreadyExistException("User with given username or email already exist");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(new ArrayList<>(Collections.singleton(new SimpleGrantedAuthority("user"))));

        userCacheService.save(user);
    }
}
