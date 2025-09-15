package pl.dayfit.dayguard.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.dayfit.dayguard.auth.JwtPrincipal;
import pl.dayfit.dayguard.dto.encryption.PreKeyBundleResponseDTO;
import pl.dayfit.dayguard.dto.encryption.UploadOpkKeyDTO;
import pl.dayfit.dayguard.dto.encryption.UploadSpkKeyDTO;
import pl.dayfit.dayguard.service.EncryptionKeyService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/encryption")
public class EncryptionController {
    private final EncryptionKeyService encryptionKeyService;

    @GetMapping("/user/{id}/get-pre-key-bundle")
    public ResponseEntity<PreKeyBundleResponseDTO> getPreKeyBundle(@PathVariable @NotNull UUID id)
    {
        return ResponseEntity.ok(encryptionKeyService
                .getPreKeyBundle(id));
    }

    @PostMapping("/upload-spk")
    public ResponseEntity<Map<String, String>> uploadSpkSecrets(@RequestBody UploadSpkKeyDTO dto, @AuthenticationPrincipal JwtPrincipal principal)
    {
        encryptionKeyService
                .handleSpkUpload(dto, principal.getName());

        return ResponseEntity
                .ok(Map.of("message", "upload went successful"));
    }

    @PostMapping("/upload-opk-keys")
    public ResponseEntity<Map<String, String>> uploadOpkKeys(@RequestBody UploadOpkKeyDTO dto, @AuthenticationPrincipal JwtPrincipal principal)
    {
        encryptionKeyService
                .handleOpkKeysUpload(dto, principal.getName());

        return ResponseEntity
                .ok(Map.of("message", "upload went successful"));
    }
}
