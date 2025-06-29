package pl.dayfit.dayguard.Helpers;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtSecretKeyService;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtService;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class KeyLocator implements Locator<Key> {
    private final JwtSecretKeyService secretKeyService;

    @Override
    public Key locate(Header header) {
        if(!(header.get(JwtService.CURRENT_SECRET_KEY_ID_HEADER) instanceof Integer secretKeyId))
        {
            return null;
        }

        return secretKeyService.getSecretKey(secretKeyId);
    }
}
