package org.estg.ipp.pt.ServerSide.Services;

import jakarta.persistence.EntityNotFoundException;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;  // Ensure this is injected correctly by Spring

    public Long saveMessage(Message message) {
        // Save the message in the database
        return messageRepository.save(message).getId();
    }

    public boolean hasUserSentApprovalRequests(String sender) {
        return messageRepository.existsBySenderAndIsApprovalRequestIsTrue(sender);
    }

    @Transactional
    public Message deleteMessageById(long id) {
        // Retrieve the entity first
        Optional<Message> messageOpt = messageRepository.findById(id);
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            messageRepository.delete(message); // Delete the entity
            return message; // Return the deleted entity
        } else {
            throw new EntityNotFoundException("Message with ID " + id + " not found.");
        }
    }

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

    public boolean isSameMessage(Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        return messageOptional.isPresent();
    }

    public List<Message> getUnreadChatMessages(String receiver, String sender) {
        return messageRepository.findMessageByIsReadIsFalseAndIsApprovalRequestIsFalseAndSenderAndReceiver(receiver, sender);
    }

    public List<Message> getPendingApprovalRequests() {
        return messageRepository.findChatMessageByIsApprovalRequestIsTrue();
    }

    public List<Message> getUnreadMessages(String receiver) {
        return messageRepository.findMessageByIsReadIsFalseAndReceiver(receiver);
    }

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
