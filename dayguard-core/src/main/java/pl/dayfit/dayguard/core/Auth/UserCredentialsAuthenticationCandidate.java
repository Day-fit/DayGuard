package pl.dayfit.dayguard.core.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class UserCredentialsAuthenticationCandidate extends AbstractAuthenticationToken {
    private final String identifier;
    private final String password;

    public UserCredentialsAuthenticationCandidate(String identifier, String password)
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
