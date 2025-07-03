package pl.dayfit.dayguard.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtService;
import pl.dayfit.dayguard.Services.Auth.Jwt.UserDetailsService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String accessToken = (String) authentication.getCredentials();
        if(!(jwtService.isValidToken(accessToken)))
        {
            throw new BadCredentialsException("Access token is invalid");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(
                jwtService.getUsername(accessToken)
        );

        return new JwtAuthenticationToken(userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
