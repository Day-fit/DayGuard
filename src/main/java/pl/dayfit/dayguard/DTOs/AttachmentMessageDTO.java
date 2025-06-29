package pl.dayfit.dayguard.DTOs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@SuperBuilder
public class AttachmentMessageDTO extends MessageDTO {
    private final List<MultipartFile> attachments;
}
