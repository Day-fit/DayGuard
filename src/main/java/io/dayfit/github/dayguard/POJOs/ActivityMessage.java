package io.dayfit.github.dayguard.POJOs;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
public class ActivityMessage extends Message {
    String targetUsername;
}
