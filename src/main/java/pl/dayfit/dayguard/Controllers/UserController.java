package pl.dayfit.dayguard.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dayfit.dayguard.Auth.JwtPrincipal;
import pl.dayfit.dayguard.DTOs.Auth.UserDetailsResponseDTO;
import pl.dayfit.dayguard.Services.UserService;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/api/v1/get-user-details")
    public ResponseEntity<UserDetailsResponseDTO> getUserDetails(@AuthenticationPrincipal JwtPrincipal principal)
    {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("User is not logged in");
        }

        return ResponseEntity.ok(userService.getUserDetailsDTO(principal.userDetails().getUsername()));
    }
}
