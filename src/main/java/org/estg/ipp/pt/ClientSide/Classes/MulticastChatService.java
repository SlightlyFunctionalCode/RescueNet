package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Interfaces.InputHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageReceiver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A classe {@code MulticastChatService} estende {@link AbstractChatService} e implementa a interface {@link MessageReceiver}.
 * Ela gerencia uma sessão de chat multicast, incluindo o envio e recebimento de mensagens, bem como o processamento de comandos do servidor.
 *
 * <p>Esta classe conecta-se a um servidor, inicia a receção de mensagens num chat multicast e permite que o utilizador envie mensagens
 * e interaja com o chat por comandos.</p>
 */
public class MulticastChatService extends AbstractChatService implements MessageReceiver {
    private final MessageHandler messageHandler;
    private final ServerMessageProcessor messageProcessor;
    private final InputHandler inputHandler;
    private final Socket serverSocket;

    /**
     * Constrói uma instância de {@code MulticastChatService} com os parâmetros necessários.
     *
     * @param groupAddress o endereço do grupo multicast.
     * @param port a porta do grupo multicast.
     * @param name o nome do usuário que inicia o chat.
     * @param serverSocket o ‘socket’ de comunicação com o servidor.
     * @param host o nome ou endereço do host do servidor.
     * @throws IOException se ocorrer um erro ao configurar o chat ou o servidor.
     */
    public MulticastChatService(String groupAddress, int port, String name, Socket serverSocket, String host) throws IOException {
        super(groupAddress, port, host, name);
        this.messageHandler = new MulticastChatManager(groupAddress, port, this.getSocket());
        this.messageProcessor = new ServerMessageProcessor();
        this.serverSocket = serverSocket;
        this.inputHandler = new UserInputHandler(new DefaultCommandHandler(host), messageHandler, this);
    }

    /**
     * Inicia a sessão de chat multicast.
     *
     * <p>Este método envia uma mensagem de preparação para o servidor, começa a enviar uma mensagem indicando que o usuário entrou no chat,
     * inicia a recepção de mensagens e processa comandos de entrada do usuário.</p>
     *
     * @param groupAddress o endereço do grupo multicast.
     * @param port a porta do grupo multicast.
     * @param name o nome do usuário que entra no chat.
     * @throws IOException se ocorrer um erro ao conectar ou enviar mensagens.
     */
    @Override
    public void startChat(String groupAddress, int port, String name) throws IOException {

        try {
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            out.println("READY:" + name);

            messageHandler.sendMessage(name + " entrou no chat!");
            messageHandler.startReceivingMessages(this);

            Scanner serverInput = new Scanner(serverSocket.getInputStream());
            new Thread(() -> {
                while (serverInput.hasNextLine()) {
                    String serverMessage = serverInput.nextLine();
                    String processedMessage = messageProcessor.processIncomingMessage(serverMessage, out);
                    System.out.println(processedMessage);
                }
            }).start();

            inputHandler.handleInput(name);
        } catch (IOException e) {
            System.err.println(Constants.ERROR_CHAT_SESSION);
        }
    }

    /**
     * Recebe uma mensagem do grupo multicast e exibe-a na consola.
     *
     * @param message a mensagem recebida.
     */
    @Override
    public void onMessageReceived(String message) {
        System.out.println(message);
    }
}