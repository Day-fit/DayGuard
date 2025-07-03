package pl.dayfit.dayguard.Schedules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.Services.Auth.Jwt.JwtSecretKeyService;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class SecretKeyRotationService {
    private final JwtSecretKeyService secretKeyService;
    private final ApplicationContext applicationContext;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    private void handleJwtSecretKeyRotation()
    {
        try {
            secretKeyService.handleRotation();
        } catch (NoSuchAlgorithmException ex) {
            log.error("Could not find given algorithm. Exception Message: {}", ex.getMessage());
            SpringApplication.exit(applicationContext, () -> -1);
        }
    }
}
