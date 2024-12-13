package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;

/**
 * A classe {@code MulticastChatManager} gere o envio e a receção de mensagens
 * num grupo de multicast para implementar a funcionalidade de chat em grupo.
 *
 * <p>Esta classe utiliza o ‘socket multicast’ para estabelecer a comunicação entre os participantes do chat.
 * As mensagens são enviadas como pacotes de dados e recebidas continuamente em uma thread separada.</p>
 */
public class MulticastChatManager implements MessageHandler {
    private final InetAddress group;
    private final int port;
    private final MulticastSocket socket;

    /**
     * Construtor para inicializar o {@code MulticastChatManager}.
     *
     * @param groupAddress o endereço do grupo multicast.
     * @param port a porta utilizada para comunicação.
     * @param socket o socket multicast configurado para o grupo.
     * @throws IOException se houver um erro ao resolver o endereço do grupo.
     */
    public MulticastChatManager(String groupAddress, int port, MulticastSocket socket) throws IOException {
        this.group = InetAddress.getByName(groupAddress);
        this.port = port;
        this.socket = socket;
    }

    /**
     * Envia uma mensagem para todos os membros do grupo multicast.
     *
     * @param message a mensagem a ser enviada.
     * @throws IOException se houver um erro durante o envio da mensagem.
     */
    public void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

    /**
     * Inicia a receção contínua de mensagens do grupo multicast.
     *
     * <p>As mensagens recebidas são encaminhadas para o método {@link MessageReceiver#onMessageReceived(String)}
     * implementado pelo recetor fornecido.</p>
     *
     * @param receiver a instância de {@link MessageReceiver} para tratar as mensagens recebidas.
     */
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
                        System.err.println(Constants.ERROR_RECEIVING_MESSAGE);
                    }
                    break;
                }
            }
        }).start();
    }
}
