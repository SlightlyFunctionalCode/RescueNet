package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;

public class MulticastChatManager implements MessageHandler {
    private final InetAddress group;
    private final int port;
    private final MulticastSocket socket;

    public MulticastChatManager(String groupAddress, int port, MulticastSocket socket) throws IOException {
        this.group = InetAddress.getByName(groupAddress);
        this.port = port;
        this.socket = socket;
    }

    public void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

    public void startReceivingMessages(MessageReceiver receiver) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (!socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    receiver.onMessageReceived(receivedMessage);
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Error receiving message: " + e.getMessage());
                    }
                    break;
                }
            }
        }).start();
    }
}
