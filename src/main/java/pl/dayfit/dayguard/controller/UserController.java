package pl.dayfit.dayguard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dayfit.dayguard.auth.JwtPrincipal;
import pl.dayfit.dayguard.dto.ActiveUserDTO;
import pl.dayfit.dayguard.dto.auth.UserDetailsResponseDTO;
import pl.dayfit.dayguard.service.MQService;
import pl.dayfit.dayguard.service.UserService;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MQService mqService;

    @GetMapping("/api/v1/get-user-details")
    public ResponseEntity<UserDetailsResponseDTO> getUserDetails(@AuthenticationPrincipal JwtPrincipal principal)
    {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("User is not logged in");
        }

        return ResponseEntity.ok(userService.getUserDetailsDTO(principal.userDetails().getUsername()));
    }

    @GetMapping("/api/v1/active-users")
    public ResponseEntity<List<ActiveUserDTO>> getActiveUsers()
    {
        return ResponseEntity.ok(
                mqService.getActiveUsersAsDTO()
        );
    }
}
