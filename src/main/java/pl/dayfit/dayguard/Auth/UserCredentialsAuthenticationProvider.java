package pl.dayfit.dayguard.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtService;
import pl.dayfit.dayguard.Services.Auth.Jwt.UserDetailsService;

@Component
@RequiredArgsConstructor
public class UserCredentialsAuthenticationProvider implements AuthenticationProvider {
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public static final long ACCESS_TOKEN_COOKIE_VALIDITY_TIME = 1000 * 60 * 15; //15 minutes
    public static final long REFRESH_TOKEN_COOKIE_VALIDITY_TIME = 1000 * 60 * 60 * 24; //24 hours

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserCredentialsAuthenticationCandidate candidate = (UserCredentialsAuthenticationCandidate) authentication;
        UserDetailsImplementation userDetails = userDetailsService.loadUserByUsername((String) candidate.getPrincipal());

        if (!passwordEncoder.matches((String) candidate.getCredentials(), userDetails.getPassword()))
        {
            throw new BadCredentialsException("Given credentials are incorrect");
        }

        return new UserCredentialsAuthenticationToken(
                userDetails,
                jwtService.generateToken(
                        userDetails.getId(),
                        ACCESS_TOKEN_COOKIE_VALIDITY_TIME
                ),

                jwtService.generateToken(
                        userDetails.getId(),
                        REFRESH_TOKEN_COOKIE_VALIDITY_TIME
                )
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UserCredentialsAuthenticationCandidate.class.isAssignableFrom(authentication);
    }
}
