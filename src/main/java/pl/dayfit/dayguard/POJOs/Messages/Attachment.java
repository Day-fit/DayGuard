package pl.dayfit.dayguard.POJOs.Messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Attachment {
    private String name;
    private String data;
    private String type;
    private Long size;
}
