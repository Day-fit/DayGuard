package pl.dayfit.dayguard.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentMessageRequestDTO extends MessageRequestDTO{
    private List<MultipartFile> attachments;
    private String receiver;
}
