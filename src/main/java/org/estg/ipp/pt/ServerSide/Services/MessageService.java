package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;  // Ensure this is injected correctly by Spring

    public void saveMessage(Message message) {
        // Save the message in the database
        messageRepository.save(message);
    }
}
