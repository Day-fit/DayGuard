package pl.dayfit.dayguard.Controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dayfit.dayguard.Configurations.Properties.CookiePropertiesConfiguration;
import pl.dayfit.dayguard.DTOs.Auth.LoginDTO;
import pl.dayfit.dayguard.DTOs.Auth.RegisterDTO;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtService;
import pl.dayfit.dayguard.Services.Cache.UserCacheService;
import pl.dayfit.dayguard.Services.UserService;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    public final String ACCESS_TOKEN_NAME = "accessToken";
    public final String REFRESH_TOKEN_NAME = "refreshToken";

    public final long ACCESS_TOKEN_COOKIE_VALIDITY_TIME = 1000 * 60 * 15; //15 minutes
    public final long REFRESH_TOKEN_COOKIE_VALIDITY_TIME = 1000 * 60 * 60 * 24; //24 hours

    private final CookiePropertiesConfiguration cookieProperties;

    private final JwtService jwtService;
    private final UserService userService;
    private final UserCacheService userCacheService;

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> handleLogin(@RequestBody @Valid LoginDTO dto, HttpServletResponse response)
    {
        String value = dto.getEmail() != null? dto.getEmail() : dto.getUsername();
        User user = userCacheService.findByEmailOrUsername(value);

        ResponseCookie accessToken = ResponseCookie.from(
                ACCESS_TOKEN_NAME,
                jwtService.generateToken(user.getId(), ACCESS_TOKEN_COOKIE_VALIDITY_TIME)
        )
                .secure(cookieProperties.isUsingSecuredCookies())
                .sameSite(cookieProperties.getSameSitePolicy())
                .httpOnly(true)
                .build();

        ResponseCookie refreshToken = ResponseCookie.from(
                REFRESH_TOKEN_NAME,
                jwtService.generateToken(user.getId(), REFRESH_TOKEN_COOKIE_VALIDITY_TIME)
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
        return ResponseEntity.ok("User has been successfully registered");
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<?> handleTokenRefresh(HttpServletResponse response, HttpServletRequest request)
    {
        Cookie refreshToken = Arrays.stream(request.getCookies())
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
                jwtService.generateToken(jwtService.extractId(refreshTokenValue), ACCESS_TOKEN_COOKIE_VALIDITY_TIME)
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
