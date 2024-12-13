package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A classe {@code Message} representa uma mensagem trocada entre dois utilizadores,
 * com as informações sobre o remetente, o destinatário, o conteúdo, e o estado de leitura e/ou
 * se é um pedido de aprovação.
 *
 * <p>Esta classe é uma entidade JPA mapeada para uma tabela na base de dados.</p>
 */
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;
    private boolean isRead = false;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    private boolean isApprovalRequest = false;

    /**
     * Construtor padrão, é necessário para o JPA.
     */
    public Message() {
    }

    /**
     * Construtor completo para facilitar a inicialização de todos os campos, exceto o {@code id} e o {@code timestamp}
     * que são gerados automaticamente.
     *
     * @param sender            O remetente da mensagem.
     * @param receiver          O destinatário da mensagem.
     * @param content           O conteúdo da mensagem.
     * @param isRead            Indica se a mensagem foi lida.
     * @param isApprovalRequest Indica se a mensagem é uma solicitação de aprovação.
     */
    public Message(String sender, String receiver, String content, boolean isRead, boolean isApprovalRequest) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = isRead;
        this.isApprovalRequest = isApprovalRequest;
    }

    /**
     * Construtor para o caso de ser um pedido de aprovação.
     *
     * @param sender            O remetente da mensagem.
     * @param receiver          O destinatário da mensagem.
     * @param content           O conteúdo da mensagem.
     * @param isApprovalRequest Indica se a mensagem é uma solicitação de aprovação.
     */
    public Message(String sender, String receiver, String content, boolean isApprovalRequest) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isApprovalRequest = isApprovalRequest;
    }

    /**
     * Construtor básico para o caso de ser uma mensagem padrão.
     *
     * @param sender   O remetente da mensagem.
     * @param receiver O destinatário da mensagem.
     * @param content  O conteúdo da mensagem.
     */
    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Devolve o identificador único da mensagem.
     *
     * @return O id da mensagem.
     */
    public Long getId() {
        return id;
    }

    /**
     * Devolve o remetente da mensagem.
     *
     * @return O remetente.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Devolve o destinatário da mensagem.
     *
     * @return O destinatário.
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Devolve o conteúdo da mensagem.
     *
     * @return O conteúdo.
     */
    public String getContent() {
        return content;
    }

    /**
     * Devolve a data/hora da mensagem.
     *
     * @return A data e hora da mensagem.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Define o remetente da mensagem.
     *
     * @param sender O remetente.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Define o destinatário da mensagem.
     *
     * @param receiver O destinatário.
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Define o conteúdo da mensagem.
     *
     * @param content O conteúdo.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Define o carimbo de data/hora da mensagem.
     *
     * @param timestamp A data e hora da mensagem.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Verifica se a mensagem foi lida.
     *
     * @return {@code true} se a mensagem foi lida, caso contrário, {@code false}.
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Define o estado de leitura da mensagem.
     *
     * @param read {@code true} se a mensagem foi lida, caso contrário, {@code false}.
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Define o identificador único da mensagem.
     *
     * @param id O id da mensagem.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Verifica se a mensagem é uma solicitação de aprovação.
     *
     * @return {@code true} se for uma solicitação de aprovação, caso contrário, {@code false}.
     */
    public boolean isApprovalRequest() {
        return isApprovalRequest;
    }

    /**
     * Define se a mensagem é uma solicitação de aprovação.
     *
     * @param approvalRequest {@code true} se for uma solicitação de aprovação, caso contrário, {@code false}.
     */
    public void setApprovalRequest(boolean approvalRequest) {
        isApprovalRequest = approvalRequest;
    }

    /**
     * Devolve a representação textual da mensagem.
     *
     * @return A ‘string’ que contém os dados da mensagem.
     */
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", isRead=" + isRead +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", isApprovalRequest=" + isApprovalRequest +
                '}';
    }
}
