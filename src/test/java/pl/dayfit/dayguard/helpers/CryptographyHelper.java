package pl.dayfit.dayguard.helpers;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CryptographyHelper {
    public byte[] generateSignature(byte[] ikPrivate, byte[] spkPublic) {
        Ed25519PrivateKeyParameters params = new Ed25519PrivateKeyParameters(ikPrivate, 0);

        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, params);
        signer.update(spkPublic, 0, spkPublic.length);

        return signer.generateSignature();
    }

    public AsymmetricCipherKeyPair generateEd25519() {
        Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
        keyPairGenerator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));

        return keyPairGenerator.generateKeyPair();
    }

    public String generateEd25519Base64(boolean isPrivate) {
        Base64.Encoder encoder = Base64.getEncoder();

        if (isPrivate) {
            return encoder
                    .encodeToString(((Ed25519PrivateKeyParameters) generateEd25519().getPrivate())
                            .getEncoded());
        }

        return encoder
                .encodeToString(((Ed25519PublicKeyParameters) generateEd25519().getPublic())
                        .getEncoded());
    }
}
