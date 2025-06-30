package pl.dayfit.dayguard.Validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.dayfit.dayguard.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.Validators.Adnotations.ValidLogin;

public class LoginDTOValidator implements ConstraintValidator<ValidLogin, LoginDTO> {
    @Override
    public boolean isValid(LoginDTO loginDTO, ConstraintValidatorContext context) {
        if (loginDTO == null) return false;

        boolean hasUsername = loginDTO.getUsername() != null && !loginDTO.getUsername().isBlank();
        boolean hasEmail = loginDTO.getEmail() != null && !loginDTO.getEmail().isBlank();
        boolean hasPassword = loginDTO.getPassword() != null && !loginDTO.getPassword().isBlank();

        return (hasUsername ^ hasEmail) && hasPassword;
    }
}
