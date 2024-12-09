package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ServerSide.Services.MessageService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

@Component
public class HandleMulticastMessages {
    public void handleMulticastMessages(int port, String address, String name, String host, MessageService service) {
        try {
            InetAddress group = InetAddress.getByName(address);
            MulticastSocket socket = new MulticastSocket(port);
            NetworkInterface networkInterface = NetworkInterface.getByName(host);

            SocketAddress groupSocketAddress = new InetSocketAddress(group, port);
            socket.joinGroup(groupSocketAddress, networkInterface);

            startReceivingMessages(socket, service, name);
        } catch (Exception e) {
            System.out.println("ERRO: Ocorreu um erro ao escutar o grupo multicast: " + address);
        }
    }

    private void startReceivingMessages(MulticastSocket socket, MessageService messageService, String name) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (!socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                    String[] splitMessage = receivedMessage.split(":");

                    System.out.println(receivedMessage);

                    String sender;
                    String content;
                    if (splitMessage.length != 2) {
                        System.out.println("ERRO: Formato inválido para mensagem multicast");

                    } else {
                        sender = splitMessage[0].trim();
                        content = splitMessage[1].trim();

                        Message message = new Message(sender, name, content, true, false);

                        messageService.saveMessage(message);
                    }
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        System.err.println(Constants.ERROR_RECEIVING_MESSAGE);
                    }
                    break;
                }
            }
        }).start();
    }
}
