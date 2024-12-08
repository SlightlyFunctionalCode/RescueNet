package org.estg.ipp.pt.ClientSide.Interfaces;

import org.estg.ipp.pt.ClientSide.Classes.AbstractChatService;

import java.io.IOException;

public interface CommandHandler {
    void handleCommand(String command, String name, AbstractChatService abstractChatService) throws IOException;
}
