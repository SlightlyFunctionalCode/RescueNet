package org.estg.ipp.pt.Classes;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    // Constructors
    public Message() {
    }

    public Message(String sender, String receiver, String content, boolean isRead, boolean isApprovalRequest) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = isRead;
        this.isApprovalRequest = isApprovalRequest;
    }

    public Message(String sender, String receiver, String content, boolean isApprovalRequest) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isApprovalRequest = isApprovalRequest;
    }

    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isApprovalRequest() {
        return isApprovalRequest;
    }

    public void setApprovalRequest(boolean approvalRequest) {
        isApprovalRequest = approvalRequest;
    }

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
