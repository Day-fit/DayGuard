package pl.dayfit.dayguard.Auth;

import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;

public record JwtPrincipal(@Getter UserDetails userDetails) implements Principal {

    @Override
    public String getName() {
        return userDetails.getUsername();
    }
}
