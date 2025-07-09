package pl.dayfit.dayguard.DTOs;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.dayfit.dayguard.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.DTOs.Auth.RegisterDTO;
import pl.dayfit.dayguard.POJOs.Messages.Attachment;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
            validator = factory.getValidator();
        }
    }

    @Test
    void testRegisterDTOValid() {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("validpassword123")
                .build();

        // When
        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(registerDTO);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRegisterDTOInvalidUsername() {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("") // Invalid: blank username
                .email("valid@example.com")
                .password("validpassword123")
                .build();

        // When
        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(registerDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username") &&
                        v.getMessage().equals("Username cannot be blank")));
    }

    @Test
    void testRegisterDTOInvalidEmail() {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("validuser")
                .email("invalid-email") // Invalid: malformed email
                .password("validpassword123")
                .build();

        // When
        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(registerDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                        v.getMessageTemplate().equals("Email is invalid")));
    }

    @Test
    void testRegisterDTOInvalidPassword() {
        // Given
        RegisterDTO registerDTO = RegisterDTO.builder()
                .username("validuser")
                .email("valid@example.com")
                .password("") // Invalid: blank password
                .build();

        // When
        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(registerDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") &&
                        v.getMessage().equals("Password cannot be blank")));
    }

    @Test
    void testLoginDTOValid() {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("validuser")
                .password("validpassword123")
                .build();

        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLoginDTOInvalidIdentifier() {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("") // Invalid: blank identifier
                .password("validpassword123")
                .build();

        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("identifier") &&
                        v.getMessage().equals("Identifier cannot be blank")));
    }

    @Test
    void testLoginDTOInvalidPassword() {
        // Given
        LoginDTO loginDTO = LoginDTO.builder()
                .identifier("validuser")
                .password("") // Invalid: blank password
                .build();

        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") &&
                        v.getMessage().equals("Password cannot be blank")));
    }

    @Test
    void testTextMessageRequestDTOValid() {
        // Given
        TextMessageRequestDTO textMessageDTO = TextMessageRequestDTO.builder()
                .receiver("validuser")
                .message("Hello, this is a valid message")
                .build();

        // When
        Set<ConstraintViolation<TextMessageRequestDTO>> violations = validator.validate(textMessageDTO);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTextMessageRequestDTOInvalidReceiver() {
        // Given
        TextMessageRequestDTO textMessageDTO = TextMessageRequestDTO.builder()
                .receiver("") // Invalid: blank receiver
                .message("Hello, this is a valid message")
                .build();

        // When
        Set<ConstraintViolation<TextMessageRequestDTO>> violations = validator.validate(textMessageDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("receiver") &&
                        v.getMessage().equals("Receiver cannot be blank")));
    }

    @Test
    void testTextMessageRequestDTOInvalidMessage() {
        // Given
        TextMessageRequestDTO textMessageDTO = TextMessageRequestDTO.builder()
                .receiver("validuser")
                .message("") // Invalid: blank message
                .build();

        // When
        Set<ConstraintViolation<TextMessageRequestDTO>> violations = validator.validate(textMessageDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("message") &&
                        v.getMessage().equals("Message cannot be blank")));
    }

    @Test
    void testAttachmentMessageRequestDTOValid() {
        // Given
        Attachment attachment = new Attachment("test.txt", "base64data", "text/plain", 1024L);
        AttachmentMessageRequestDTO attachmentMessageDTO = AttachmentMessageRequestDTO.builder()
                .receiver("validuser")
                .attachments(List.of(attachment))
                .build();

        // When
        Set<ConstraintViolation<AttachmentMessageRequestDTO>> violations = validator.validate(attachmentMessageDTO);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testAttachmentMessageRequestDTOInvalidReceiver() {
        // Given
        Attachment attachment = new Attachment("test.txt", "base64data", "text/plain", 1024L);
        AttachmentMessageRequestDTO attachmentMessageDTO = AttachmentMessageRequestDTO.builder()
                .receiver("") // Invalid: blank receiver
                .attachments(List.of(attachment))
                .build();

        // When
        Set<ConstraintViolation<AttachmentMessageRequestDTO>> violations = validator.validate(attachmentMessageDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("receiver") &&
                        v.getMessage().equals("Receiver cannot be blank")));
    }

    @Test
    void testAttachmentMessageRequestDTONullAttachments() {
        // Given
        AttachmentMessageRequestDTO attachmentMessageDTO = AttachmentMessageRequestDTO.builder()
                .receiver("validuser")
                .attachments(null) // Invalid: null attachments
                .build();

        // When
        Set<ConstraintViolation<AttachmentMessageRequestDTO>> violations = validator.validate(attachmentMessageDTO);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("attachments") &&
                        v.getMessage().equals("Attachments cannot be null")));
    }
} 