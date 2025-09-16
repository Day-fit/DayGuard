package pl.dayfit.dayguard.auth;

import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.UUID;

public record JwtPrincipal(@Getter UserDetails userDetails, UUID id) implements Principal {
    @Override
    public String getName() {
        return userDetails.getUsername();
    }

    public UUID getId()
    {
        return id;
    }
}
