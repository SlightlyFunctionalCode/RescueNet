package org.estg.ipp.pt.ClientSide.Interfaces;

import java.io.IOException;

public interface MessageHandler {
    void sendMessage(String message) throws IOException;

    void startReceivingMessages(MessageReceiver receiver);
}
