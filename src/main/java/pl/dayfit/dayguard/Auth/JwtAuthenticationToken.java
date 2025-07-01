package pl.dayfit.dayguard.Auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private @Setter String jwtAccessToken;
    private @Getter @Setter String jwtRefreshToken;
    private final String identifier;

    public JwtAuthenticationToken(String identifier, String jwtAccessToken, String jwtRefreshToken, Collection<? extends GrantedAuthority> roles)
    {
        super(roles);

        this.identifier = identifier;
        this.jwtAccessToken = jwtAccessToken;
        this.jwtRefreshToken = jwtRefreshToken;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwtAccessToken;
    }

    @Override
    public Object getPrincipal() {
        return identifier;
    }
}
