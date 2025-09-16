package pl.dayfit.dayguard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.dto.auth.RegisterDTO;
import pl.dayfit.dayguard.dto.auth.UserDetailsResponseDTO;
import pl.dayfit.dayguard.entity.OpkPublicKey;
import pl.dayfit.dayguard.entity.User;
import pl.dayfit.dayguard.exception.InvalidSignatureException;
import pl.dayfit.dayguard.exception.UserAlreadyExistException;
import pl.dayfit.dayguard.repository.UserRepository;
import pl.dayfit.dayguard.service.cache.UserCacheService;
import pl.dayfit.dayguard.type.OpkStatus;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserCacheService userCacheService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionKeyService encryptionKeyService;

    public void register(RegisterDTO dto)
    {
        String username = dto.getUsername();
        String email = dto.getEmail();

        byte[] ikPublicKey = Base64.getDecoder().decode(dto.getIkPub());
        byte[] spkPublicKey = Base64.getDecoder().decode(dto.getSpkPub());
        byte[] spkSignature = Base64.getDecoder().decode(dto.getSpkSignature());

        List<byte[]> opkPublicKeys = dto.getOpkPubs()
                .stream()
                .map(opk -> Base64.getDecoder().decode(opk))
                .toList();

        if (userRepository.existsByEmailOrUsername(email, username))
        {
            throw new UserAlreadyExistException("User with given username or email already exist");
        }

        if (encryptionKeyService.isInvalidSignature(spkSignature, ikPublicKey, spkPublicKey))
        {
            throw new InvalidSignatureException("Invalid signature");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(new ArrayList<>(Collections.singleton(new SimpleGrantedAuthority("user"))));
        user.setIkPub(ikPublicKey);
        user.setSpkPub(spkPublicKey);
        user.setSpkSignature(spkSignature);

        User savedUser = userCacheService.save(user); //Saving to get ID

        savedUser.getOpkPubs()
            .addAll(
                    opkPublicKeys
                            .stream()
                            .map(opk -> new OpkPublicKey(null, opk, OpkStatus.ACTIVE, savedUser.getId()))
                            .toList()
        );

        userCacheService.save(savedUser);
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
