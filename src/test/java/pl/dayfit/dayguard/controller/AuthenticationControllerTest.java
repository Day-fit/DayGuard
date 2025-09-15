package pl.dayfit.dayguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.dayfit.dayguard.dto.auth.LoginDTO;
import pl.dayfit.dayguard.dto.auth.RegisterDTO;
import pl.dayfit.dayguard.helpers.CryptographyHelper;
import pl.dayfit.dayguard.repository.UserRepository;

import java.util.Base64;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CryptographyHelper cryptographyHelper;

    private MockMvc mockMvc;

    private RegisterDTO registerDTO;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        AsymmetricCipherKeyPair keyPair = cryptographyHelper.generateEd25519();

        byte[] ikPrivate = ((Ed25519PrivateKeyParameters) keyPair.getPrivate()).getEncoded();
        byte[] ikPublic = ((Ed25519PublicKeyParameters) keyPair.getPublic()).getEncoded();

        byte[] spkPublic = ((Ed25519PublicKeyParameters) cryptographyHelper.generateEd25519().getPublic()).getEncoded();
        byte[] spkSignature = cryptographyHelper.generateSignature(ikPrivate, spkPublic);

        registerDTO = RegisterDTO.builder()
                .username("detailsuser")
                .email("details@example.com")
                .password("password123")
                .spkPub(Base64.getEncoder().encodeToString(spkPublic))
                .spkSignature(Base64.getEncoder().encodeToString(spkSignature))
                .ikPub(Base64.getEncoder().encodeToString(ikPublic))
                .opkPubs(List.of(cryptographyHelper.generateEd25519Base64(false)))
                .build();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User has been successfully registered"));
    }

    @Test
    void testRegisterWithDuplicateUsername() throws Exception {
        // Given
        RegisterDTO secondUser = registerDTO;
        secondUser.setUsername("detailsuser");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with given username or email already exist"));
    }

    @Test
    void testRegisterWithDuplicateEmail() throws Exception {
        // Given
        RegisterDTO secondUser = registerDTO;
        secondUser.setEmail("details@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with given username or email already exist"));
    }

    @Test
    void testRegisterWithInvalidData() throws Exception {
        // Given
        RegisterDTO invalidUser = registerDTO;
        invalidUser.setUsername(""); // Invalid: empty username
        invalidUser.setEmail("invalid-email"); // Invalid: malformed email
        invalidUser.setPassword("123"); // Invalid: too short password

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Register user first
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        // Then login
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("detailsuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("nonexistent")
                .password("wrongpassword")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithEmail() throws Exception {
        // Register user first
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        // Then login with email
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("details@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void testLogout() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().value("refreshToken", ""));
    }

    @Test
    void testRefreshToken() throws Exception {
          mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("detailsuser")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        String refreshTokenValue = setCookieHeaders.stream()
                .filter(header -> header.startsWith("refreshToken="))
                .map(header -> header.substring("refreshToken=".length(), header.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("refreshToken cookie not found"));

        // When & Then - Try to refresh token
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshTokenValue)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"));
    }
} 