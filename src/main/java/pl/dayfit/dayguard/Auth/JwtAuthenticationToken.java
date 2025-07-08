package pl.dayfit.dayguard.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.security.Principal;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String accessToken;
    private final Principal principal;

    public JwtAuthenticationToken(String accessToken)
    {
        super(null);
        this.accessToken = accessToken;
        this.principal = null;
        setAuthenticated(false);
    }

    public JwtAuthenticationToken(JwtPrincipal principal)
    {
        super(principal.userDetails().getAuthorities());
        this.accessToken = null;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
