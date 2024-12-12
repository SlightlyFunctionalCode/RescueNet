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
 * Esta interface estende o {@link JpaRepository}, fornecendo métodos para realizar operações CRUD
 * básicas sobre o repositório de mensagens. Além disso, fornece consultas personalizadas para encontrar
 * mensagens com base em critérios específicos, como leitura, solicitações de aprovação, destinatário,
 * remetente, e grupos de chat.
 *
 * <p>A interface {@link MessageRepository} permite:</p>
 * <ul>
 *     <li>Buscar mensagens não lidas entre um remetente e um destinatário específico.</li>
 *     <li>Buscar mensagens não lidas para um destinatário específico.</li>
 *     <li>Buscar mensagens de chat que são solicitações de aprovação.</li>
 *     <li>Buscar as últimas mensagens de chat de um grupo, limitadas a 30.</li>
 *     <li>Verificar se um remetente tem solicitações de aprovação pendentes.</li>
 * </ul>
 *
 * <p>O repositório utiliza o Spring Data JPA, facilitando o acesso ao banco de dados com consultas personalizadas.</p>
 *
 * @see Message
 * @see JpaRepository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Encontra mensagens não lidas para um destinatário específico.
     *
     * @param receiver O destinatário das mensagens.
     * @return Uma lista de mensagens que não foram lidas pelo destinatário especificado.
     */
    List<Message> findMessageByIsReadIsFalseAndReceiver(String receiver);

    /**
     * Encontra mensagens de chat que são solicitações de aprovação.
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

    /**
     * Verifica se um remetente tem mensagens de aprovação pendentes.
     *
     * @param sender O remetente das mensagens.
     * @return {@code true} se existir pelo menos uma mensagem de solicitação de aprovação pendente do remetente,
     *         caso contrário, {@code false}.
     */
    boolean existsBySenderAndIsApprovalRequestIsTrue(String sender);
}
