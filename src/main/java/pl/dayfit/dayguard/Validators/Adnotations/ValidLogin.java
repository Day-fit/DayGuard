package pl.dayfit.dayguard.Validators.Adnotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.dayfit.dayguard.Validators.LoginDTOValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LoginDTOValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLogin {
    String message() default "Login request body is invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
