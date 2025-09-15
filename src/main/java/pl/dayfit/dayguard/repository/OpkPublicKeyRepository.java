package pl.dayfit.dayguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dayfit.dayguard.entity.OpkPublicKey;
import pl.dayfit.dayguard.type.OpkStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpkPublicKeyRepository extends JpaRepository<OpkPublicKey, Long> {
    List<OpkPublicKey> findByUploaderIdAndStatus(UUID uploaderId, OpkStatus status);
}
