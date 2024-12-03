package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;
import org.estg.ipp.pt.ServerSide.Services.MulticastManagerService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastChatService extends AbstractChatService {
    private final MulticastManagerService multicastManagerService;
    private final CommandHandler commandHandler;
    private final Socket serverSocket;

    public MulticastChatService(String groupAddress, int port, String name,
                                MulticastManagerService multicastManagerService,
                                CommandHandler commandHandler, Socket serverSocket) throws IOException {
        super(groupAddress, port, name);
        this.multicastManagerService = multicastManagerService;
        this.commandHandler = commandHandler;
        this.serverSocket = serverSocket;
    }


    @Override
    public void startChat(String groupAddress, int port, String name) throws IOException {

        try {
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            out.println("READY:" + name);
            System.out.println("READY sent to server");

            MulticastManager multicastManager = multicastManagerService.getOrCreateMulticastManager(groupAddress, port);

            multicastManager.sendMessage(name + " entrou no chat!");

            Scanner serverInput = new Scanner(serverSocket.getInputStream());
            new Thread(() -> {
                while (serverInput.hasNextLine()) {
                    String serverMessage = serverInput.nextLine();
                    String processedMessage = handleIncomingMessage(serverMessage, out);

                    System.out.println("**" + processedMessage + "**");
                }
            }).start();

            multicastManager.receiveMessages();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite a mensagem (/logout para sair): ");
            boolean shouldExit = false;

            while (!shouldExit) {
                String msg = scanner.nextLine();
                if (msg.equalsIgnoreCase("/logout")) {
                    commandHandler.handleCommand("/logout", name);
                    shouldExit = true;
                    multicastManager.close();

                } else if (msg.startsWith("/")) {
                    commandHandler.handleCommand(msg, name);
                } else {
                    multicastManager.sendMessage(name + ":" + msg);
                }
            }

        } catch (IOException e) {
            System.err.println("Error during chat session: " + e.getMessage());
        }
    }

    public String handleIncomingMessage(String message, PrintWriter out) {
        Matcher messageMatcher = RegexPatterns.MESSAGE.matcher(message);
        if (messageMatcher.matches()) {
            String messageId = messageMatcher.group("id");

            if (messageId != null) {
                // Send an isRead confirmation to the server
                messageId = messageId.replace("/", "");
                sendIsReadConfirmation(messageId, out);

                Pattern pattern = Pattern.compile("/.+?/"); // Matches text enclosed by single slashes

                return message.replaceAll(pattern.pattern(), "");
            }
        }
        return null;
    }

    private void sendIsReadConfirmation(String messageId, PrintWriter out) {
        String confirmationMessage = "CONFIRM_READ:" + messageId;
        out.println(confirmationMessage);
    }
}
