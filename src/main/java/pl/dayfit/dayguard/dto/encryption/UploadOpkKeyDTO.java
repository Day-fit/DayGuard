package pl.dayfit.dayguard.dto.encryption;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UploadOpkKeyDTO {
    private List<String> opkKeys;
}
