package pl.dayfit.dayguard.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationCandidate extends AbstractAuthenticationToken {
    private final String identifier;
    private final String password;

    public JwtAuthenticationCandidate(String identifier, String password)
    {
        super(null);

        this.identifier = identifier;
        this.password = password;

        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return identifier;
    }
}
