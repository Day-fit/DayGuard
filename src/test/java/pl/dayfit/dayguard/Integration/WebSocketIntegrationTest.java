package pl.dayfit.dayguard.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pl.dayfit.dayguard.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.DTOs.Auth.RegisterDTO;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(response -> response.getStatusCode().is5xxServerError());
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testUserRegistrationAndLogin() {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .password("password123")
                .build();

        // When - Register user
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register",
                createHttpEntity(registerDTO),
                String.class
        );

        // Then
        assertEquals(200, registerResponse.getStatusCode().value());

        // When - Login user
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("integrationuser")
                .password("password123")
                .build();

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login",
                createHttpEntity(loginDTO),
                String.class
        );

        // Then
        assertEquals(200, loginResponse.getStatusCode().value());
        assertNotNull(loginResponse.getHeaders().get("Set-Cookie"));
    }

    @Test
    void testGetUserDetails() {
        // Given - Register and login first
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("detailsuser")
                .email("details@example.com")
                .password("password123")
                .build();

        restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register",
                createHttpEntity(registerDTO),
                String.class
        );

        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("detailsuser")
                .password("password123")
                .build();

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login",
                createHttpEntity(loginDTO),
                String.class
        );

        // Extract cookies from login response
        List<String> rawCookies = Objects.requireNonNull(loginResponse.getHeaders().get("Set-Cookie"));

        String cookieHeader = rawCookies.stream()
                .map(cookie -> cookie.split(";", 2)[0])
                .collect(Collectors.joining("; "));

        // When - Get user details
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookieHeader);

        ResponseEntity<String> userDetailsResponse = restTemplate.exchange(
                baseUrl + "/api/v1/get-user-details",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        );

        // Then
        assertEquals(200, userDetailsResponse.getStatusCode().value());
        assertNotNull(userDetailsResponse.getBody());
        assertTrue(userDetailsResponse.getBody().contains("detailsuser"));
        assertTrue(userDetailsResponse.getBody().contains("details@example.com"));
    }

    @Test
    void testTokenRefresh() {
        // Given - Register and login first
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("refreshuser")
                .email("refresh@example.com")
                .password("password123")
                .build();

        restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register",
                createHttpEntity(registerDTO),
                String.class
        );

        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("refreshuser")
                .password("password123")
                .build();

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login",
                createHttpEntity(loginDTO),
                String.class
        );

        // Extract cookies from login response
        List<String> cookies = loginResponse.getHeaders().get("Set-Cookie");
        assertNotNull(cookies);

        String headerCookies = cookies.stream()
                .map(cookie -> cookie.split(";", 2)[0])
                .collect(Collectors.joining("; "));

        // When - Refresh token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", headerCookies);

        ResponseEntity<String> refreshResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/refresh",
                new HttpEntity<>(null, headers),
                String.class
        );

        // Then
        assertEquals(200, refreshResponse.getStatusCode().value());
        assertNotNull(refreshResponse.getHeaders().get("Set-Cookie"));
    }

    @Test
    void testUnauthorizedAccess() {
        // When - Try to access protected endpoint without authentication
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/get-user-details",
                String.class
        );

        // Then
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testInvalidLogin() {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("nonexistent")
                .password("wrongpassword")
                .build();

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/login",
                createHttpEntity(loginDTO),
                String.class
        );

        // Then
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testDuplicateRegistration() {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("duplicateuser")
                .email("duplicate@example.com")
                .password("password123")
                .build();

        // When - Register first time
        ResponseEntity<String> firstResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register",
                createHttpEntity(registerDTO),
                String.class
        );

        // Then
        assertEquals(200, firstResponse.getStatusCode().value());

        // When - Try to register again with same username
        ResponseEntity<String> secondResponse = restTemplate.postForEntity(
                baseUrl + "/api/v1/auth/register",
                createHttpEntity(registerDTO),
                String.class
        );

        // Then
        assertEquals(409, secondResponse.getStatusCode().value());
    }

    private HttpEntity<String> createHttpEntity(Object object) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new HttpEntity<>(objectMapper.writeValueAsString(object), headers);
        } catch (Exception e) {
            throw new RuntimeException("Error creating HTTP entity", e);
        }
    }
} 