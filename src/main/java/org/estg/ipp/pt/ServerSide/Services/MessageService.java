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
    private MessageRepository messageRepository;

    public Long saveMessage(Message message) {
        return messageRepository.save(message).getId();
    }

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

    public String getContent(Long id) {
        return messageRepository.findById(id).get().getContent();
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
