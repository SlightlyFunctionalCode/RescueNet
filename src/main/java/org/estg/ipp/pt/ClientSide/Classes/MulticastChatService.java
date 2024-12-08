package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Interfaces.InputHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageReceiver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MulticastChatService extends AbstractChatService implements MessageReceiver {
    private final MessageHandler messageHandler;
    private final ServerMessageProcessor messageProcessor;
    private final InputHandler inputHandler;
    private final Socket serverSocket;

    public MulticastChatService(String groupAddress, int port, String name, Socket serverSocket, String host) throws IOException {
        super(groupAddress, port, host, name);
        this.messageHandler = new MulticastChatManager(groupAddress, port, this.getSocket());
        this.messageProcessor = new ServerMessageProcessor();
        this.serverSocket = serverSocket;
        this.inputHandler = new UserInputHandler(new DefaultCommandHandler(host), messageHandler, this);
    }

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
                    System.out.println("**" + processedMessage + "**");
                }
            }).start();

            inputHandler.handleInput(name);
        } catch (IOException e) {
            System.err.println("Error during chat session: " + e.getMessage());
        }
    }

    @Override
    public void onMessageReceived(String message) {
        System.out.println(message);
    }
}