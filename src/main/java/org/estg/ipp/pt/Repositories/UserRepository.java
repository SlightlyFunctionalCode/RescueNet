package org.estg.ipp.pt.Repositories;

import org.estg.ipp.pt.Classes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdentifier(String identifier);
}
