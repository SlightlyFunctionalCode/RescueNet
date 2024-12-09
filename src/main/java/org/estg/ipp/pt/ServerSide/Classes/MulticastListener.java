package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Services.MessageService;
import org.springframework.stereotype.Component;

import java.net.*;

@Component
public class MulticastListener {
    public void handleMulticastMessages(Group group, String host, MessageService service) {
        try {
            InetAddress groupAddress = InetAddress.getByName(group.getAddress());
            MulticastSocket socket = new MulticastSocket(group.getPort());
            NetworkInterface networkInterface = NetworkInterface.getByName(host);

            SocketAddress groupSocketAddress = new InetSocketAddress(groupAddress, group.getPort());
            socket.joinGroup(groupSocketAddress, networkInterface);

            startReceivingMessages(socket, service, group.getName());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("ERRO: Ocorreu um erro ao escutar o grupo multicast: " + group.getName());
        }
    }

    private void startReceivingMessages(MulticastSocket socket, MessageService messageService, String name) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];

            System.out.println("A escutar: " + name);

            while (!socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                    String[] splitMessage = receivedMessage.split(":");

                    String sender;
                    String content;
                    if (splitMessage.length != 2) {
                        if (!receivedMessage.startsWith("/")) {
                            System.out.println("ERRO: Formato inválido para mensagem multicast");
                        }
                    } else {
                        sender = splitMessage[0].trim();
                        content = splitMessage[1].trim();

                        if (!sender.startsWith(">")) {
                            Message message = new Message(sender, name, content, true, false);

                            messageService.saveMessage(message);
                        }
                    }
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        System.err.println("Erro: Ocorreu um erro");
                    }
                    break;
                }
            }
        }).start();
    }
}
