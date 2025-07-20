package pl.dayfit.dayguard.core.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.core.DTOs.Auth.RegisterDTO;
import pl.dayfit.dayguard.core.DTOs.Auth.UserDetailsResponseDTO;
import pl.dayfit.dayguard.core.Entities.User;
import pl.dayfit.dayguard.core.Exceptions.UserAlreadyExistException;
import pl.dayfit.dayguard.core.Repositories.UserRepository;
import pl.dayfit.dayguard.core.Services.Cache.UserCacheService;

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

    public UserDetailsResponseDTO getUserDetailsDTO(String identifier)
    {
        User user = userCacheService.findByEmailOrUsername(identifier);

        return UserDetailsResponseDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
