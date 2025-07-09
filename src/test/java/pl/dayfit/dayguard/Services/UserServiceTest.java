package pl.dayfit.dayguard.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Repositories.UserRepository;
import pl.dayfit.dayguard.Services.Cache.UserCacheService;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserCacheService cacheService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setRoles(Collections.singletonList(new SimpleGrantedAuthority("user")));
    }

    @Test
    void testCreateUser() {
        // When
        User createdUser = cacheService.save(testUser);

        // Then
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("test@example.com", createdUser.getEmail());
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