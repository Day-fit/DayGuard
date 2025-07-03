package pl.dayfit.dayguard.Services.Auth.Jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.Entities.User;
import pl.dayfit.dayguard.Events.SecretKeyRotatedEvent;
import pl.dayfit.dayguard.Helpers.KeyLocator;
import pl.dayfit.dayguard.Services.Cache.UserCacheService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    public static String CURRENT_SECRET_KEY_ID_HEADER = "secretKeyId";

    private final JwtSecretKeyService secretKeyService;
    private final KeyLocator keyLocator;
    private final UserCacheService userCacheService;
    private volatile Key currentSecretKey;
    private volatile int currentSecretKeyId;

    /**
     * Updates currentSecretKey field when the secret key rotation is completed
     * <p>
     *     Acts as a cache - reduce redundant calls to the secretKeyService
     * </p>
     */
    @EventListener
    @SuppressWarnings("unused")
    private void updateSecretKey(SecretKeyRotatedEvent _event)
    {
        currentSecretKey = secretKeyService.getCurrentSecretKey();
        currentSecretKeyId = secretKeyService.getCurrentSecretKeyId();
    }


    /**
     * Generates JWT token based on params
     *
     * @param userId User's ID
     * @param validityTime Time that JWT token will be valid (in millis)
     * @return JWT token encoded to String
     */
    public String generateToken(Long userId, long validityTime)
    {
        User user = userCacheService.findById(userId);

        if (validityTime <= 0)
        {
            throw new IllegalArgumentException("validityTime parameter can not be smaller than zero!");
        }

        Map<String, Integer> headers = new HashMap<>();
        headers.put(CURRENT_SECRET_KEY_ID_HEADER, currentSecretKeyId);

        return Jwts.builder()
                .header()
                .add(headers)
                .and()
                .signWith(currentSecretKey)
                .subject(String.valueOf(userId))
                .claim("username", user.getUsername())
                .claim("roles", user.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validityTime))
                .compact();
    }

    public boolean isValidToken(String token)
    {
        return isNotExpired(token) && extractId(token) != null;
    }

    @SuppressWarnings("unused")
    public boolean isValidToken(String token, Integer userId)
    {
        return isOwner(token, userId) && isNotExpired(token);
    }

    public String getUsername(String token)
    {
        String username = extractClaims(token, claims -> claims.get("username", String.class));

        if (username == null)
        {
            throw new IllegalArgumentException("Given token is invalid");
        }

        return username;
    }

    private boolean isOwner(String token, Integer userId)
    {
        String ownerRaw = extractClaims(token, Claims::getSubject);

        if (ownerRaw == null)
        {
            return false;
        }

        return ownerRaw.equals(userId.toString());
    }

    private boolean isNotExpired(String token)
    {
        Date expiration = extractClaims(token, Claims::getExpiration);

        if (expiration == null)
        {
            return false;
        }

        return expiration.after(new Date());
    }

    public Long extractId(String token)
    {
        String rawId = extractClaims(token, Claims::getSubject);

        if (rawId == null) {
            return null;
        }

        try{
            return Long.valueOf(rawId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver)
    {
        final Claims claims = extractAllClaims(token);

        if (claims == null)
        {
            return null;
        }

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token)
    {
        try {
            return Jwts.parser()
                    .keyLocator(keyLocator)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception exception) {
            log.debug("Could not able to extract claims from JWT token");
            return null;
        }
    }
}
