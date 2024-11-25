package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Interfaces.ChatService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class AbstractChatService implements ChatService {
    protected volatile boolean running = true;
    protected InetAddress group;
    protected MulticastSocket socket;
    protected String name;

    public AbstractChatService(String groupAddress, int port, String name) throws IOException {
        this.group = InetAddress.getByName(groupAddress);
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(group);
        this.name = name;
    }

    @Override
    public void stopChat() {
        running = false;
        if (socket != null) {
            socket.close();
        }
    }
}