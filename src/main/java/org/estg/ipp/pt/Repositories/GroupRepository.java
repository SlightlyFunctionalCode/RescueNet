package org.estg.ipp.pt.Repositories;

import org.estg.ipp.pt.Classes.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // Encontrar grupo pelo nome
    Optional<Group> findByName(String name);

    // Verificar se um grupo existe pelo nome
    boolean existsByName(String name);
}
