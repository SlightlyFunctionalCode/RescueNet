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

/**
 * Interface de repositório para a entidade {@link Message}.
 *
 * <p>Esta interface estende o {@link JpaRepository} e  fornece os métodos para realizar as operações CRUD
 * básicas sobre o repositório de mensagens. Além disso, fornece as consultas personalizadas para encontrar as
 * mensagens com base em critérios específicos, como o estado de leitura, se é uma solicitações de aprovação, o destinatário,
 * o remetente, e pelos grupos de chat.</p>
 *
 * <p>A interface {@link MessageRepository} permite:</p>
 * <ul>
 *     <li>Buscar as mensagens não lidas para um destinatário específico.</li>
 *     <li>Buscar as mensagens de chat que são solicitações de aprovação.</li>
 *     <li>Buscar as últimas mensagens de chat de um grupo, limitadas a 30.</li>
 * </ul>
 *
 * @see Message
 * @see JpaRepository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Encontra as mensagens não lidas para um destinatário específico.
     *
     * @param receiver O destinatário das mensagens.
     * @return Uma lista de mensagens que não foram lidas pelo destinatário especificado.
     */
    List<Message> findMessageByIsReadIsFalseAndReceiver(String receiver);

    /**
     * Encontra as mensagens de chat que são solicitações de aprovação.
     *
     * @return Uma lista de mensagens que são solicitações de aprovação.
     */
    List<Message> findChatMessageByIsApprovalRequestIsTrue();

    /**
     * Encontra as últimas 30 mensagens de chat enviadas para um grupo específico,
     * ordenadas pela data de envio (timestamp).
     *
     * @param groupName O nome do grupo de chat.
     * @return Uma lista das últimas 30 mensagens enviadas para o grupo, ordenadas por timestamp.
     */
    @Query("SELECT m FROM Message m WHERE m.receiver = :groupName ORDER BY m.timestamp ASC LIMIT 30")
    List<Message> findLatestChatMessagesByGroup(
           String groupName
    );
}
