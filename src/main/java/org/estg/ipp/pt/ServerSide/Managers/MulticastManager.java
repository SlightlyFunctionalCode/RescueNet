package org.estg.ipp.pt.ServerSide.Managers;

import java.io.IOException;
import java.net.*;

/**
 * A classe {@code MulticastManager} gerencia a comunicação via multicast usando um socket multicast.
 *
 * <p>Esta classe fornece os métodos necessários para enviar e receber mensagens em um grupo multicast
 * especificado por um endereço IP de grupo e uma porta. Ela utiliza a classe {@link MulticastSocket} para
 * estabelecer a comunicação com o grupo e gerenciar o envio e recebimento de pacotes.</p>
 *
 * <p>Principais funcionalidades:</p>
 * <ul>
 *     <li>Enviar mensagens para o grupo multicast.</li>
 *     <li>Receber mensagens do grupo multicast.</li>
 *     <li>Entrar e sair de um grupo multicast de forma controlada.</li>
 * </ul>
 *
 * @see MulticastSocket
 * @see DatagramPacket
 */
public class MulticastManager {
    /**
     * Endereço IP do grupo multicast.
     */
    private final String groupAddress;

    /**
     * Porta do grupo multicast.
     */
    private final int port;

    /**
     * O socket utilizado para enviar e receber pacotes multicast.
     */
    private final MulticastSocket socket;

    /**
     * O endereço IP do grupo multicast como um {@link InetAddress}.
     */
    private final InetAddress group;

    /**
     * Constrói um {@code MulticastManager} com o endereço e a porta do grupo multicast.
     *
     * @param groupAddress O endereço IP do grupo multicast.
     * @param port A porta do grupo multicast.
     * @throws IOException Se ocorrer um erro ao criar o socket ou ao se conectar ao grupo multicast.
     */
    public MulticastManager(String groupAddress, int port) throws IOException {
        this.groupAddress = groupAddress;
        this.port = port;
        this.socket = new MulticastSocket(port);  // Usar MulticastSocket em vez de DatagramSocket
        socket.setReuseAddress(true);
        this.group = InetAddress.getByName(groupAddress);

        // Fazer o socket "entrar" no grupo multicast
        socket.joinGroup(new InetSocketAddress(group, port).getAddress());
    }

    /**
     * Envia uma mensagem para o grupo multicast.
     *
     * @param message A mensagem a ser enviada.
     * @throws IOException Se ocorrer um erro ao enviar o pacote.
     */
    public void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

    /**
     * Inicia uma nova thread para receber mensagens do grupo multicast.
     * As mensagens recebidas serão exibidas no console.
     */
    public void receiveMessages() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Fecha o socket multicast e sai do grupo multicast.
     *
     * @throws IOException Se ocorrer um erro ao sair do grupo ou fechar o socket.
     */
    public void close() throws IOException {
        socket.leaveGroup(group);  // Deixa o grupo ao fechar
        socket.close();
    }

    /**
     * Retorna o endereço do grupo multicast.
     *
     * @return O endereço do grupo multicast.
     */
    public String getGroupAddress() {
        return groupAddress;
    }

    /**
     * Retorna a porta do grupo multicast.
     *
     * @return A porta do grupo multicast.
     */
    public int getPort() {
        return port;
    }
}
