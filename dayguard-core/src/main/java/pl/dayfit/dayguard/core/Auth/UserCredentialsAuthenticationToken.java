package pl.dayfit.dayguard.core.Auth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class UserCredentialsAuthenticationToken extends AbstractAuthenticationToken {
    private final String jwtAccessToken;
    @Getter
    private final String jwtRefreshToken;
    private final UserDetails userDetails;

    public UserCredentialsAuthenticationToken(UserDetails userDetails, String jwtAccessToken, String jwtRefreshToken)
    {
        super(userDetails.getAuthorities());

        this.jwtAccessToken = jwtAccessToken;
        this.jwtRefreshToken = jwtRefreshToken;
        this.userDetails = userDetails;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwtAccessToken;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }
}
