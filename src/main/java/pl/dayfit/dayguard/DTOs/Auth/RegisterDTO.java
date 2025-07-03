package pl.dayfit.dayguard.DTOs.Auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    @NotNull
    private String email;
    @NotNull
    private String username;
    @NotNull
    private String password;
}
