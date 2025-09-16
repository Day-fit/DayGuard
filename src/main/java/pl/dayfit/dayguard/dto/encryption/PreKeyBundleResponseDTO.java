package pl.dayfit.dayguard.dto.encryption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PreKeyBundleResponseDTO {
    private String ikPub;
    private String spkPub;
    private String spkSignature;
    private String opkKey;
}
