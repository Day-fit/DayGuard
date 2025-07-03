package pl.dayfit.dayguard.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.dayfit.dayguard.Auth.JwtAuthenticationProvider;
import pl.dayfit.dayguard.Auth.JwtAuthenticationToken;
import pl.dayfit.dayguard.Controllers.AuthenticationController;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieJwtFilter extends OncePerRequestFilter {
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        if (cookies == null)
        {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie accessTokenCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equalsIgnoreCase(AuthenticationController.ACCESS_TOKEN_NAME))
                .findFirst()
                .orElse(null);

        if (accessTokenCookie == null)
        {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessTokenValue = accessTokenCookie.getValue();
            JwtAuthenticationToken authToken = (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(new JwtAuthenticationToken(accessTokenValue));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (AuthenticationException ex) {
          log.debug("Authentication failed. IP: {}, reason: {}", request.getRemoteAddr(), ex.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) || request.getMethod().equalsIgnoreCase("OPTIONS");
    }
}
