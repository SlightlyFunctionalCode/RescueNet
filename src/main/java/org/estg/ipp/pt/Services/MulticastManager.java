package org.estg.ipp.pt.Services;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;

import java.io.IOException;
import java.net.*;

public class MulticastManager {
    private final String groupAddress;
    private final int port;
    private final MulticastSocket socket;
    private final InetAddress group;

    public MulticastManager(String groupAddress, int port) throws IOException {
        this.groupAddress = groupAddress;
        this.port = port;
        this.socket = new MulticastSocket(port);  // Usar MulticastSocket em vez de DatagramSocket
        socket.setReuseAddress(true);
        this.group = InetAddress.getByName(groupAddress);

        // Fazer o socket "entrar" no grupo multicast
        socket.joinGroup(new InetSocketAddress(group, port).getAddress());
    }

    public void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

    public void receiveMessages(MessageHandler messageHandler) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    messageHandler.handleMessage(receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void close() throws IOException {
        socket.leaveGroup(group);  // Deixa o grupo ao fechar
        socket.close();
    }

    public String getGroupAddress() {
        return groupAddress;
    }

    public int getPort() {
        return port;
    }
}
