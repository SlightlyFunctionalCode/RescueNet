package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void updateContent(String content, Long messageId) {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setContent(content);
            messageRepository.save(message);
        } else {
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
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
