package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Interfaces.ChatService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

/**
 * A classe abstrata {@code AbstractChatService} fornece uma implementação parcial para um serviço de chat
 * multicast, e permite a comunicação em grupo por ‘sockets’ multicast.
 *
 * <p>Esta classe implementa a ‘interface’ {@link ChatService} e gere a configuração e o controlo
 * de um {@link MulticastSocket} para a comunicação num grupo multicast.</p>
 *
 * <p>As subclasses devem fornecer as implementações específicas para os métodos definidos na interface {@code ChatService}.</p>
 */
public abstract class AbstractChatService implements ChatService {
    private InetAddress group;
    private MulticastSocket socket;
    private String name;
    private String host;
    private int port;
    private final NetworkInterface networkInterface;

    /**
     * Construtor para inicializar o serviço de chat multicast.
     *
     * @param groupAddress o endereço IP do grupo multicast.
     * @param port a porta usada para comunicação multicast.
     * @param host o nome da ‘interface’ de rede utilizada.
     * @param name o nome do utilizador associado ao serviço.
     * @throws IOException se ocorrer um erro ao configurar o socket ou juntar-se ao grupo multicast.
     */
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

    /**
     * Fecha o serviço de chat ao sair do grupo multicast e ao fechar o socket multicast.
     */
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

    /**
     * Devolve o endereço do grupo multicast.
     *
     * @return o endereço do grupo multicast.
     */
    public InetAddress getGroup() {
        return group;
    }

    /**
     * Define o endereço do grupo multicast.
     *
     * @param group o novo endereço do grupo multicast.
     */
    public void setGroup(InetAddress group) {
        this.group = group;
    }

    /**
     * Devolve o socket multicast.
     *
     * @return o socket multicast.
     */
    public MulticastSocket getSocket() {
        return socket;
    }

    /**
     * Define o socket multicast.
     *
     * @param socket o novo socket multicast.
     */
    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    /**
     * Devolve o nome do utilizador associado ao serviço de chat.
     *
     * @return o nome do utilizador.
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome do utilizador associado ao serviço de chat.
     *
     * @param name o novo nome do usuário.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Devolve o nome do host ou ‘interface’ de rede utilizada.
     *
     * @return o nome do host ou interface.
     */
    public String getHost() {
        return host;
    }

    /**
     * Define o nome do host ou ‘interface’ de rede utilizada.
     *
     * @param host o novo nome do host ou interface.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Devolve a porta usada para comunicação multicast.
     *
     * @return a porta utilizada.
     */
    public int getPort() {
        return port;
    }

    /**
     * Define a porta usada para comunicação multicast.
     *
     * @param port a nova porta.
     */
    public void setPort(int port) {
        this.port = port;
    }
}
