package org.estg.ipp.pt.ServerSide.Repositories;

import org.estg.ipp.pt.Classes.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
}
