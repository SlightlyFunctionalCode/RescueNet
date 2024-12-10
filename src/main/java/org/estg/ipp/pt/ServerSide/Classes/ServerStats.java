package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Server;
import org.springframework.stereotype.Component;

@Component
public class ServerStats {
    private int totalCommandsExecuted = 0;

    public synchronized void incrementCommandsExecuted() {
        totalCommandsExecuted++;
    }

    public synchronized int getTotalCommandsExecuted() {
        return totalCommandsExecuted;
    }

    public synchronized int getConnectedUsers() {
        return Server.getNumberOfClients();
    }
}
