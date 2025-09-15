package pl.dayfit.dayguard.service;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.stereotype.Service;
import pl.dayfit.dayguard.dto.encryption.PreKeyBundleResponseDTO;
import pl.dayfit.dayguard.dto.encryption.UploadOpkKeyDTO;
import pl.dayfit.dayguard.dto.encryption.UploadSpkKeyDTO;
import pl.dayfit.dayguard.entity.OpkPublicKey;
import pl.dayfit.dayguard.entity.User;
import pl.dayfit.dayguard.exception.InvalidSignatureException;
import pl.dayfit.dayguard.service.cache.UserCacheService;
import pl.dayfit.dayguard.type.OpkStatus;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EncryptionKeyService {
    private final UserCacheService userCacheService;

    public PreKeyBundleResponseDTO getPreKeyBundle(UUID id) {
        Base64.Encoder encoder = Base64.getEncoder();

        User user = userCacheService.findById(id);
        List<OpkPublicKey> opkPubs = user.getOpkPubs();
        OpkPublicKey opkPub = opkPubs.isEmpty() ? null : opkPubs.getFirst();

        if (opkPub == null)
        {
            return new PreKeyBundleResponseDTO(
                    encoder.encodeToString(user.getIkPub()),
                    encoder.encodeToString(user.getSpkPub()),
                    encoder.encodeToString(user.getSpkSignature()),
                    null
            );
        }

        opkPub.setStatus(OpkStatus.CONSUMED);
        opkPubs.set(0, opkPub);

        user.setOpkPubs(opkPubs);
        userCacheService.save(user);

        return new PreKeyBundleResponseDTO(
                encoder.encodeToString(user.getIkPub()),
                encoder.encodeToString(user.getSpkPub()),
                encoder.encodeToString(user.getSpkSignature()),
                encoder.encodeToString(opkPub.getKey())
        );
    }

    public boolean isInvalidSignature(byte[] signature, byte[] ikPub, byte[] spkPub) {
        Ed25519PublicKeyParameters publicKeyParameters = new Ed25519PublicKeyParameters(ikPub, 0);

        Ed25519Signer verifier = new Ed25519Signer();
        verifier.init(false, publicKeyParameters);
        verifier.update(spkPub, 0, spkPub.length);

        return !verifier.verifySignature(signature);
    }

    public void handleSpkUpload(UploadSpkKeyDTO dto, String name) {
        User user = userCacheService.findByUsername(name);

        Base64.Decoder decoder = Base64.getDecoder();

        byte[] spkSignature = decoder.decode(dto.getSpkSignature());
        byte[] spkPublicKey =  decoder.decode(dto.getSpkPublicKey());

        if (isInvalidSignature(spkSignature, user.getIkPub(), spkPublicKey))
        {
            throw new InvalidSignatureException("Invalid signature");
        }

        user.setSpkSignature(spkSignature);
        user.setSpkPub(spkPublicKey);

        userCacheService.save(user);
    }

    public void handleOpkKeysUpload(UploadOpkKeyDTO dto, String name) {
        User user = userCacheService.findByUsername(name);

        List<OpkPublicKey> opkKeys = dto.getOpkKeys()
                .stream()
                .map(opk -> Base64.getDecoder().decode(opk))
                .map(opk -> new OpkPublicKey(null, opk, OpkStatus.ACTIVE, user.getId()))
                .toList();

        user.setOpkPubs(opkKeys);
        userCacheService.save(user);
    }
}
