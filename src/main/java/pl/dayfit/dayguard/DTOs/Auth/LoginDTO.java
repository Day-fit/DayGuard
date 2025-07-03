package pl.dayfit.dayguard.DTOs.Auth;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDTO {
    @NotNull
    private String identifier;
    @NotNull
    private String password;
}
