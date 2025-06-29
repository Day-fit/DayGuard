package pl.dayfit.dayguard.DTOs.Auth;

import lombok.Builder;
import lombok.Getter;
import pl.dayfit.dayguard.Validators.Adnotations.ValidLogin;

@Getter
@Builder
@ValidLogin
public class LoginDTO {
    private String username;
    private String email;
    private String password;
}
