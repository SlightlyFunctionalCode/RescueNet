package org.estg.ipp.pt.ClientSide.Interfaces;

import java.io.IOException;

public interface CommandHandler {
    void handleCommand(String command, String name) throws IOException;
}
