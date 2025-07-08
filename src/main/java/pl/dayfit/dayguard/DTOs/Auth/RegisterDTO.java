package pl.dayfit.dayguard.DTOs.Auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 254, min = 6, message = "Email must be in range of 6-254 characters")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Username cannot be blank")
    @Size(max = 20, min = 3, message = "Username length must be in range of 3-20 characters")
    @Pattern(regexp="^[A-Za-z0-9]+$", message="Username must be alphanumeric only")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(max = 64, min = 8, message = "Password must be in range of 8-64 characters")
    private String password;
}
