package io.dayfit.github.dayguard.POJOs;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder
public class ActivityMessage extends Message {
    private String targetUsername;
    private List<String> targetUsernames;
}
