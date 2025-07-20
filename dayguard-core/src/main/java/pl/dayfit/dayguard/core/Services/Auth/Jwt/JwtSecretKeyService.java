package pl.dayfit.dayguard.core.Services.Auth.Jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dayfit.dayguard.core.Events.SecretKeyRotatedEvent;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtSecretKeyService {
    private final AtomicInteger currentSecretKeyId = new AtomicInteger(-1);
    private final ConcurrentMap<Integer, Key> secretKeysHistory = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void handleRotation() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");

        int MAX_SECRET_KEYS = 14;
        int newIndex = (currentSecretKeyId.get() + 1) % MAX_SECRET_KEYS;

        secretKeysHistory.put(
                newIndex,
                keyGenerator.generateKey()
        );

        currentSecretKeyId.set(newIndex);
        applicationEventPublisher.publishEvent(new SecretKeyRotatedEvent());
    }

    public Key getCurrentSecretKey()
    {
        return secretKeysHistory.get(currentSecretKeyId.get());
    }

    public int getCurrentSecretKeyId()
    {
        return currentSecretKeyId.get();
    }

    public Key getSecretKey(int id)
    {
        return secretKeysHistory.get(id);
    }
}
