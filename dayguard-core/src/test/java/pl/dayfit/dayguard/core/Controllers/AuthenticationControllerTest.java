package pl.dayfit.dayguard.core.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import pl.dayfit.dayguard.core.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.core.DTOs.Auth.RegisterDTO;

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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .build();

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
        RegisterDTO firstUser = RegisterDTO.builder()
                .username("duplicateuser")
                .email("first@example.com")
                .password("password123")
                .build();

        RegisterDTO secondUser = RegisterDTO.builder()
                .username("duplicateuser")
                .email("second@example.com")
                .password("password456")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
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
        RegisterDTO firstUser = RegisterDTO.builder()
                .username("firstuser")
                .email("duplicate@example.com")
                .password("password123")
                .build();

        RegisterDTO secondUser = RegisterDTO.builder()
                .username("seconduser")
                .email("duplicate@example.com")
                .password("password456")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
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
        RegisterDTO invalidUser = RegisterDTO.builder()
                .username("") // Invalid: empty username
                .email("invalid-email") // Invalid: malformed email
                .password("123") // Invalid: too short password
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("loginuser")
                .email("loginuser@example.com")
                .password("password123")
                .build();

        // Register user first
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        // Then login
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("loginuser")
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
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("emailuser")
                .email("emailuser@example.com")
                .password("password123")
                .build();

        // Register user first
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        // Then login with email
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("emailuser@example.com")
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
        // Given - First register and login to get tokens
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("refreshuser")
                .email("refreshuser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk());

        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("refreshuser")
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