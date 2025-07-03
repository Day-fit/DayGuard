package pl.dayfit.dayguard.DTOs;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MessageDTO {
    protected final String receiver;
}