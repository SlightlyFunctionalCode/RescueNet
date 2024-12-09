package org.estg.ipp.pt.ServerSide.Repositories;

import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findMessageByIsReadIsFalseAndIsApprovalRequestIsFalseAndSenderAndReceiver(String receiver, String sender);

    List<Message> findMessageByIsReadIsFalseAndReceiver(String receiver);

    List<Message> findChatMessageByIsApprovalRequestIsTrue();

    @Query("SELECT m FROM Message m WHERE m.receiver = :groupName ORDER BY m.timestamp ASC LIMIT 10")
    List<Message> findLatestChatMessagesByGroup(
           String groupName
    );

    boolean existsBySenderAndIsApprovalRequestIsTrue(String sender);
}
