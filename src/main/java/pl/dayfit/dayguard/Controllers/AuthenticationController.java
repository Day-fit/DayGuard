package pl.dayfit.dayguard.Controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dayfit.dayguard.Auth.JwtAuthenticationCandidate;
import pl.dayfit.dayguard.Auth.JwtAuthenticationProvider;
import pl.dayfit.dayguard.Auth.JwtAuthenticationToken;
import pl.dayfit.dayguard.Configurations.Properties.CookiePropertiesConfiguration;
import pl.dayfit.dayguard.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.DTOs.Auth.RegisterDTO;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtService;
import pl.dayfit.dayguard.Services.UserService;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    public final String ACCESS_TOKEN_NAME = "accessToken";
    public final String REFRESH_TOKEN_NAME = "refreshToken";

    private final CookiePropertiesConfiguration cookieProperties;

    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationProvider jwtAuthenticationProvider;

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> handleLogin(@RequestBody @Valid LoginDTO dto, HttpServletResponse response)
    {
        String identifier = dto.getEmail() != null? dto.getEmail() : dto.getUsername();
        String password = dto.getPassword();

        JwtAuthenticationCandidate candidate = new JwtAuthenticationCandidate(identifier, password);
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(candidate);

        ResponseCookie accessToken = ResponseCookie.from(
                ACCESS_TOKEN_NAME,
                (String) authenticationToken.getCredentials()
        )
                .secure(cookieProperties.isUsingSecuredCookies())
                .sameSite(cookieProperties.getSameSitePolicy())
                .httpOnly(true)
                .build();

        ResponseCookie refreshToken = ResponseCookie.from(
                REFRESH_TOKEN_NAME,
                authenticationToken.getJwtRefreshToken()
        )
                .secure(cookieProperties.isUsingSecuredCookies())
                .sameSite(cookieProperties.getSameSitePolicy())
                .httpOnly(true)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessToken.toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshToken.toString()
        );

        return ResponseEntity.ok(Map.of("message","User has been logged in"));
    }

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<?> handleRegistration(@RequestBody @Valid RegisterDTO dto)
    {
        userService.register(dto);
        return ResponseEntity.ok(Map.of("message", "User has been successfully registered"));
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<?> handleTokenRefresh(HttpServletResponse response, HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies == null)
        {
            throw new IllegalArgumentException("No cookies has been found");
        }

        Cookie refreshToken = Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_NAME))
            .findFirst()
            .orElse(null);

        if (refreshToken == null)
        {
            throw new IllegalArgumentException("No refresh token cookie has been found");
        }

        String refreshTokenValue = refreshToken.getValue();

        if(jwtService.isValidToken(refreshTokenValue))
        {
            throw new BadCredentialsException("Invalid refresh token");
        }

        ResponseCookie accessToken = ResponseCookie.from(
                ACCESS_TOKEN_NAME,
                jwtService.generateToken(jwtService.extractId(refreshTokenValue), JwtAuthenticationProvider.ACCESS_TOKEN_COOKIE_VALIDITY_TIME)
        )
                .secure(cookieProperties.isUsingSecuredCookies())
                .sameSite(cookieProperties.getSameSitePolicy())
                .httpOnly(true)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessToken.toString()
        );

        return ResponseEntity.ok(Map.of("message", "Access token has been refreshed"));
    }
}
