package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Interfaces.ChatService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

public abstract class AbstractChatService implements ChatService {
    private InetAddress group;
    private MulticastSocket socket;
    private String name;
    private String host;
    private int port;
    private final NetworkInterface networkInterface;

    public AbstractChatService(String groupAddress, int port, String host, String name) throws IOException {
        this.group = InetAddress.getByName(groupAddress);
        this.socket = new MulticastSocket(port);
        this.networkInterface = NetworkInterface.getByName(host);

        SocketAddress groupSocketAddress = new InetSocketAddress(group, port);
        this.socket.joinGroup(groupSocketAddress, networkInterface);

        this.name = name;
        this.host = host;
        this.port = port;
    }

    @Override
    public void stopChat() {
        if (socket != null) {
            try {
                SocketAddress groupSocketAddress = new InetSocketAddress(group, port);
                socket.leaveGroup(groupSocketAddress, networkInterface);
            } catch (IOException e) {
                System.err.println(Constants.ERROR_LEAVING_CHAT);
            } finally {
                socket.close();
            }
        }
    }

    public InetAddress getGroup() {
        return group;
    }

    public void setGroup(InetAddress group) {
        this.group = group;
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
