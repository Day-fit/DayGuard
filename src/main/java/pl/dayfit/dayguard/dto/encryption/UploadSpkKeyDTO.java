package pl.dayfit.dayguard.dto.encryption;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadSpkKeyDTO {
    private String spkPublicKey;
    private String spkSignature;
}
