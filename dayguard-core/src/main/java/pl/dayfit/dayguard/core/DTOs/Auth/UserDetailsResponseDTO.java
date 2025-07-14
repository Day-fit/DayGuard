package pl.dayfit.dayguard.core.DTOs.Auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailsResponseDTO {
    private String username;
    private String email;
}
