package pl.dayfit.dayguard.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String accessToken;
    private final UserDetails userDetails;

    public JwtAuthenticationToken(String accessToken)
    {
        super(null);
        this.accessToken = accessToken;
        this.userDetails = null;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(UserDetails userDetails)
    {
        super(userDetails.getAuthorities());
        this.accessToken = null;
        this.userDetails = userDetails;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }
}
