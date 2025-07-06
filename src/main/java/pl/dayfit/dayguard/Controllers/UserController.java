package pl.dayfit.dayguard.Controllers;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dayfit.dayguard.DTOs.Auth.UserDetailsResponseDTO;
import pl.dayfit.dayguard.Events.UserReadyForMessagesEvent;
import pl.dayfit.dayguard.Services.UserService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserService userService;

    @GetMapping("/api/v1/connection-ready")
    public ResponseEntity<Map<String, String>> handlePing(@NotNull @AuthenticationPrincipal UserDetails userDetails)
    {
        applicationEventPublisher.publishEvent(new UserReadyForMessagesEvent(userDetails.getUsername()));
        return ResponseEntity.ok(Map.of("message", "ack"));
    }

    @GetMapping("/api/v1/get-user-details")
    public ResponseEntity<UserDetailsResponseDTO> getUserDetails(@NotNull @AuthenticationPrincipal UserDetails userDetails)
    {
        return ResponseEntity.ok(userService.getUserDetailsDTO(userDetails.getUsername()));
    }
}
