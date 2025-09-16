package pl.dayfit.dayguard.service;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import pl.dayfit.dayguard.entity.OpkPublicKey;
import pl.dayfit.dayguard.entity.User;
import pl.dayfit.dayguard.helpers.CryptographyHelper;
import pl.dayfit.dayguard.repository.UserRepository;
import pl.dayfit.dayguard.service.cache.UserCacheService;
import pl.dayfit.dayguard.type.OpkStatus;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private CryptographyHelper helper;

    @Autowired
    private UserCacheService cacheService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        AsymmetricCipherKeyPair ikCipherPair = helper.generateEd25519();
        AsymmetricCipherKeyPair spkCipherPair = helper.generateEd25519();

        byte[] ikPub = ((Ed25519PublicKeyParameters) ikCipherPair.getPublic()).getEncoded();
        byte[] ikPrivate = ((Ed25519PrivateKeyParameters) ikCipherPair.getPrivate()).getEncoded();

        byte[] spkPub = ((Ed25519PublicKeyParameters) spkCipherPair.getPublic()).getEncoded();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setSpkPub(spkPub);
        testUser.setIkPub(ikPub);
        testUser.setSpkSignature(helper.generateSignature(ikPrivate, spkPub));

        testUser.setRoles(Collections.singletonList(new SimpleGrantedAuthority("user")));

        testUser = cacheService.save(testUser);

        testUser.setOpkPubs(List.of(new OpkPublicKey(null, spkPub, OpkStatus.ACTIVE, testUser.getId())));
        cacheService.save(testUser);
    }

    @Test
    void testCreateUser() {
        assertNotNull(testUser);
        assertNotNull(testUser.getId());
        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    void testCreateUserWithDuplicateUsername() {
        // Given
        cacheService.save(testUser);

        // When & Then
        User duplicatedUser = new User();
        duplicatedUser.setUsername("testuser");
        duplicatedUser.setEmail("test1@example.com");
        duplicatedUser.setPassword("hashedPassword123");

        assertThrows(Exception.class, () -> {
            cacheService.save(duplicatedUser);
            userRepository.flush();
        });
    }

    @Test
    void testCreateUserWithDuplicateEmail() {
        // Given
        cacheService.save(testUser);

        // When & Then
        User duplicatedUser = new User();
        duplicatedUser.setUsername("testusername321");
        duplicatedUser.setEmail("test@example.com");
        duplicatedUser.setPassword("hashedPassword123");
        duplicatedUser.setRoles(Collections.singletonList(new SimpleGrantedAuthority("user")));

        assertThrows(RuntimeException.class, () -> {
            cacheService.save(duplicatedUser);
            userRepository.flush();
        });
    }

    @Test
    void testFindByUsername() {
        // Given
        cacheService.save(testUser);

        // When
        User foundUser = cacheService.findByUsername("testuser");

        // Then
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testFindByUsernameNotFound() {
        // When & Then
        assertThrows(NoSuchElementException.class, () -> cacheService.findByUsername("nonexistent"));
    }

    @Test
    void testFindByEmail() {
        // Given
        cacheService.save(testUser);

        // When
        User foundUser = cacheService.findByEmail("test@example.com");

        // Then
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testThrowsExceptionWhenNotFound() {
        //When & Then
        assertThrows(NoSuchElementException.class,
                () -> cacheService.findByEmail("nonexistent@example.com")
        );
    }
} 