package org.estg.ipp.pt.ServerSide.Services;

import jakarta.persistence.EntityNotFoundException;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * <p><strong>Serviço responsável por gerenciar operações relacionadas a mensagens.</strong></p>
 *
 * <p>Esta classe fornece funcionalidades para manipular mensagens, incluindo operações
 * de criação, leitura, atualização e exclusão (CRUD). Além disso, oferece métodos
 * específicos para recuperar mensagens não lidas, pendentes de aprovação e mensagens
 * associadas a grupos.</p>
 *
 * <h3>Funcionalidades principais:</h3>
 * <ul>
 *     <li>Salvar novas mensagens.</li>
 *     <li>Excluir mensagens existentes pelo ID.</li>
 *     <li>Atualizar o conteúdo de mensagens.</li>
 *     <li>Recuperar conteúdo de mensagens.</li>
 *     <li>Identificar mensagens não lidas ou pendentes de aprovação.</li>
 *     <li>Gerenciar o status de leitura de mensagens.</li>
 * </ul>
 *
 * <p>A classe utiliza o repositório {@code MessageRepository} para interagir com a base de dados
 * e é anotada com {@code @Service}, indicando que faz parte da camada de serviços da aplicação.</p>
 */
@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Salva uma nova mensagem no repositório.
     *
     * @param message A mensagem a ser salva.
     * @return O ID da mensagem salva.
     */
    public Long saveMessage(Message message) {
        return messageRepository.save(message).getId();
    }

    /**
     * Exclui uma mensagem com base no ID fornecido.
     *
     * @param id O ID da mensagem a ser excluída.
     * @return A mensagem que foi excluída.
     * @throws EntityNotFoundException Se nenhuma mensagem for encontrada com o ID fornecido.
     */
    @Transactional
    public Message deleteMessageById(long id) {
        Optional<Message> messageOpt = messageRepository.findById(id);
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            messageRepository.delete(message);
            return message;
        } else {
            throw new EntityNotFoundException("Message with ID " + id + " not found.");
        }
    }

    /**
     * Obtém o conteúdo de uma mensagem pelo seu ID.
     *
     * @param id O ID da mensagem.
     * @return O conteúdo da mensagem.
     * @throws EntityNotFoundException Se nenhuma mensagem for encontrada com o ID fornecido.
     */
    public String getContent(Long id) {
        return messageRepository.findById(id).get().getContent();
    }

    /**
     * Atualiza o conteúdo de uma mensagem existente.
     *
     * @param content O novo conteúdo da mensagem.
     * @param messageId O ID da mensagem a ser atualizada.
     * @return A mensagem atualizada.
     * @throws IllegalArgumentException Se nenhuma mensagem for encontrada com o ID fornecido.
     */
    public Message updateContent(String content, Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setContent(content);
            return messageRepository.save(message);
        } else {
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
    }

    /**
     * Verifica se uma mensagem existe pelo seu ID.
     *
     * @param messageId O ID da mensagem a ser verificada.
     * @return true se a mensagem existir, false caso contrário.
     */
    public boolean isSameMessage(Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        return messageOptional.isPresent();
    }

    /**
     * Recupera uma lista de mensagens que estão pendentes de aprovação.
     *
     * @return Uma lista de mensagens pendentes de aprovação.
     */
    public List<Message> getPendingApprovalRequests() {
        return messageRepository.findChatMessageByIsApprovalRequestIsTrue();
    }

    /**
     * Recupera uma lista de mensagens não lidas para um receptor específico.
     *
     * @param receiver O receptor das mensagens.
     * @return Uma lista de mensagens não lidas para o receptor especificado.
     */
    public List<Message> getUnreadMessages(String receiver) {
        return messageRepository.findMessageByIsReadIsFalseAndReceiver(receiver);
    }

    /**
     * Recupera as mensagens mais recentes de um grupo específico.
     *
     * @param groupName O nome do grupo.
     * @return Uma lista das mensagens mais recentes do grupo.
     */
    public List<Message> getLastestGroupMessages(String groupName) {
        return messageRepository.findLatestChatMessagesByGroup(groupName);
    }

    /**
     * Marca uma mensagem como lida com base no seu ID.
     *
     * @param messageId O ID da mensagem a ser marcada como lida.
     * @throws IllegalArgumentException Se nenhuma mensagem for encontrada com o ID fornecido.
     */
    public void markAsRead(Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setRead(true);
            messageRepository.save(message);
        } else {
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
    }
}
