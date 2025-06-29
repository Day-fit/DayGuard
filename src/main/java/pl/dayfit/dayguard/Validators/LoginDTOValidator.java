package pl.dayfit.dayguard.Validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.dayfit.dayguard.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.Validators.Adnotations.ValidLogin;

public class LoginDTOValidator implements ConstraintValidator<ValidLogin, LoginDTO> {
    @Override
    public boolean isValid(LoginDTO loginDTO, ConstraintValidatorContext constraintValidatorContext) {
        String username = loginDTO.getUsername();
        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();

        boolean hasValidUsername = username == null || username.isBlank();
        boolean hasValidEmail = email == null || email.isBlank();
        boolean hasValidPassword = password == null || password.isBlank();

        return (hasValidUsername || hasValidEmail) && hasValidPassword;
    }
}
