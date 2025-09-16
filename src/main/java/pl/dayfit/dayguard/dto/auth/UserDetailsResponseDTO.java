package pl.dayfit.dayguard.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailsResponseDTO {
    private String username;
    private String email;
}
