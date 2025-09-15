package pl.dayfit.dayguard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dayfit.dayguard.type.OpkStatus;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class OpkPublicKey {
    @Id
    private Long id;
    private byte[] key;
    private OpkStatus status;
    private UUID uploaderId;
}
