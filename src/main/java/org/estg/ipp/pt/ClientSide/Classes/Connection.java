package org.estg.ipp.pt.ClientSide.Classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A classe {@code Connection} gere uma conexão cliente-servidor, TCP, baseada em ‘sockets’,
 * e permite a troca de mensagens entre o cliente e o servidor.
 *
 * <p>Esta classe fornece os métodos para se efetuar a conexão, a re-conexão e para fechar a conexão, bem como
 * para acessar as streams de entrada e saída associados ao ‘socket’.</p>
 */
public class Connection {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String serverIP;
    private final int serverPort;

    /**
     * Construtor que inicializa uma conexão com o servidor especificado.
     *
     * @param serverAddress o endereço do servidor ao qual conectar.
     * @param serverPort a porta do servidor ao qual conectar.
     * @throws IOException se ocorrer um erro ao abrir o ‘socket’ ou os streams.
     */
    public Connection(String serverAddress, int serverPort) throws IOException {
        this.serverIP = serverAddress;
        this.serverPort = serverPort;
        connect(serverAddress, serverPort);
    }

    /**
     * Estabelece uma conexão com o servidor especificado.
     *
     * @param serverAddress o endereço do servidor ao qual conectar.
     * @param serverPort a porta do servidor ao qual conectar.
     * @throws IOException se ocorrer um erro ao abrir o ‘socket’ ou os streams.
     */
    public void connect(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Reconecta ao servidor através do {@code serverIP} e o {@code serverPort}.
     *
     * <p>O método fecha a conexão existente antes de tentar reconectar.</p>
     */
    public void reconnect() {
        try {
            socket.close();
            connect(serverIP, serverPort);
        } catch (IOException e) {
            System.out.println("Erro ao tentar reconectar: " + e.getMessage());
        }
    }

    /**
     * Fecha a conexão com o servidor, e fecha o ‘socket’.
     *
     * @throws IOException se ocorrer um erro ao fechar o socket.
     */
    public void close() throws IOException {
        socket.close();
    }

    /**
     * Devolve a stream de entrada associado ao ‘socket’.
     *
     * @return a stream de entrada para leitura de mensagens do servidor.
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * Devolve a stream de saída associado ao ‘socket’.
     *
     * @return a stream de saída para envio de mensagens ao servidor.
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * Devolve o ‘socket’ utilizado para a conexão.
     *
     * @return o ‘socket’ da conexão.
     */
    public Socket getSocket() {
        return socket;
    }
}
