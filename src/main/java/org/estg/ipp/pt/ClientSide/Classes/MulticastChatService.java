package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastChatService extends AbstractChatService {
    private final CommandHandler commandHandler;
    private final Socket serverSocket;

    public MulticastChatService(String groupAddress, int port, String name, Socket serverSocket, String host) throws IOException {
        super(groupAddress, port, host, name);
        this.commandHandler = new DefaultCommandHandler(host);
        this.serverSocket = serverSocket;
    }

    @Override
    public void startChat(String groupAddress, int port, String name) throws IOException {

        try {
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            out.println("READY:" + name);
            System.out.println("READY sent to server");


            sendMessage(name + " entrou no chat!");

            Scanner serverInput = new Scanner(serverSocket.getInputStream());
            new Thread(() -> {
                while (serverInput.hasNextLine()) {
                    String serverMessage = serverInput.nextLine();
                    String processedMessage = handleIncomingMessage(serverMessage, out);

                    System.out.println("**" + processedMessage + "**");
                }
            }).start();

            receiveMessages();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite a mensagem (/logout para sair): ");
            boolean shouldExit = false;

            while (!shouldExit) {
                String msg = scanner.nextLine();
                if (msg.equalsIgnoreCase("/logout")) {
                    shouldExit = true;
                    this.stopChat();
                } else if (msg.startsWith("/")) {
                    commandHandler.handleCommand(msg, name, this);
                } else {
                    sendMessage(name + ":" + msg);
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
                messageId = messageId.replace("/", "");
                sendIsReadConfirmation(messageId, out);

                Pattern pattern = Pattern.compile("/.+?/");

                return message.replaceAll(pattern.pattern(), "");
            }
        }
        return message;
    }

    private void sendIsReadConfirmation(String messageId, PrintWriter out) {
        String confirmationMessage = "CONFIRM_READ:" + messageId;
        out.println(confirmationMessage);
    }

    public void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }

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
                    System.out.println("Erro ao receber mensagem");
                    return;
                }
            }
        }).start();
    }
}
