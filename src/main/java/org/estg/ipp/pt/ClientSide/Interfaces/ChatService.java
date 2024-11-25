package org.estg.ipp.pt.ClientSide.Interfaces;

import java.io.IOException;

public interface ChatService {
    void startChat(String groupAddress, int port, String name) throws IOException;
    void stopChat();
}
