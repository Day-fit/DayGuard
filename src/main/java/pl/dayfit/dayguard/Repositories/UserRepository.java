package pl.dayfit.dayguard.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dayfit.dayguard.Entities.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    /**
     * Finds for user with username or email
     * @param value param that is compared to username and email in a query
     * @return Result of the query
     */
    @Query("SELECT u FROM User u WHERE u.username = :value OR u.email = :value")
    Optional<User> findByEmailOrUsername(String value);

    boolean existsByEmailOrUsername(String email, String username);
}