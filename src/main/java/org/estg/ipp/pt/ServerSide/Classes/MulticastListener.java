package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Services.MessageService;
import org.springframework.stereotype.Component;

import java.net.*;

/**
 * Classe responsável por escutar as mensagens de multicast num grupo específico e processá-las.
 *
 * <p>Esta classe permite escutar as mensagens de grupos multicast, receber as mensagens enviadas e processá-las
 * para armazená-las via o serviço de mensagens. Ela também gere a conexão com o grupo multicast, o que inclui
 * a criação de um ‘socket’ de multicast e a gestão de ‘interface’ de rede.</p>
 */
@Component
public class MulticastListener {

    /**
     * Método para iniciar a escuta de mensagens multicast de um grupo.
     *
     * <p>Este método cria um socket multicast e conecta-se ao grupo especificado, associando-se a uma interface de rede,
     * e começa a receber e a processar as mensagens enviadas ao grupo.</p>
     *
     * @param group O grupo multicast ao qual se deseja conectar.
     * @param host O nome da interface de rede que será utilizada para o multicast.
     * @param service O serviço de mensagens utilizado para salvar as mensagens recebidas.
     */
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

    /**
     * Método responsável por iniciar a receção das mensagens do socket multicast em uma nova thread.
     *
     * <p>Este método recebe as mensagens enviadas para o grupo multicast e processa-as para depois armazenar
     * no serviço de mensagens. Isto apenas é feito caso estas não sejam mensagens previamente lidas pelo server, de forma
     * a não salvar mensagens duplicadas na base de dados.</p>
     *
     * @param socket O socket multicast que está ouvindo as mensagens.
     * @param messageService O serviço responsável por salvar as mensagens recebidas.
     * @param name O nome do grupo multicast que está sendo escutado.
     */
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
