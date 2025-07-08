package pl.dayfit.dayguard.DTOs.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDTO {
    @NotBlank(message = "Identifier cannot be blank")
    private String identifier;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
