package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageReceiver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MulticastChatService extends AbstractChatService implements MessageReceiver {
    private final CommandHandler commandHandler;
    private final ChatNetworkManager networkManager;
    private final MessageProcessor messageProcessor;
    private final Socket serverSocket;

    public MulticastChatService(String groupAddress, int port, String name, Socket serverSocket, String host) throws IOException {
        super(groupAddress, port, host, name);
        this.commandHandler = new DefaultCommandHandler(host);
        this.networkManager = new ChatNetworkManager(groupAddress, port, this.getSocket());
        this.messageProcessor = new MessageProcessor();
        this.serverSocket = serverSocket;
    }

    @Override
    public void startChat(String groupAddress, int port, String name) throws IOException {

        try {
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            out.println("READY:" + name);

            networkManager.sendMessage(name + " entrou no chat!");
            networkManager.startReceivingMessages(this);

            Scanner serverInput = new Scanner(serverSocket.getInputStream());
            new Thread(() -> {
                while (serverInput.hasNextLine()) {
                    String serverMessage = serverInput.nextLine();
                    String processedMessage = messageProcessor.processIncomingMessage(serverMessage, out);
                    System.out.println("**" + processedMessage + "**");
                }
            }).start();

            handleUserInput(name);
        } catch (IOException e) {
            System.err.println("Error during chat session: " + e.getMessage());
        }
    }

    private void handleUserInput(String name) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite a mensagem (/logout para sair): ");
        boolean shouldExit = false;

        while (!shouldExit) {
            String msg = scanner.nextLine();
            if (msg.equalsIgnoreCase("/logout")) {
                shouldExit = true;
                this.stopChat();
            } else if (msg.startsWith("/")) {
                try {
                    commandHandler.handleCommand(msg, name, this);
                } catch (IOException e) {
                    System.out.println("Erro ao executar comando");
                }
            } else {
                try {
                    networkManager.sendMessage(name + ":" + msg);
                } catch (IOException e) {
                    System.out.println("Erro ao mandar mensagem");
                }
            }
        }
    }

    @Override
    public void onMessageReceived(String message) {
        System.out.println(message);
    }
}