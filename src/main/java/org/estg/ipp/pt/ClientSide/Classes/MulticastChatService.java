package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastChatService extends AbstractChatService {
    private final MessageHandler messageHandler;
    private final CommandHandler commandHandler;

    public MulticastChatService(String groupAddress, int port, String name, MessageHandler messageHandler, CommandHandler commandHandler) throws IOException {
        super(groupAddress, port, name);
        this.messageHandler = messageHandler;
        this.commandHandler = commandHandler;
    }

    @Override
    public void startChat(String groupAddress, int port, String name) throws IOException {
        Thread receiveThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    messageHandler.handleMessage(received);
                } catch (IOException e) {
                    if (!running) {
                        break;
                    }
                    throw new RuntimeException(e);
                }
            }
        });
        receiveThread.start();

        Scanner scanner = new Scanner(System.in);
        System.out.println("JOIN ON CHAT " + groupAddress + ": ");
        boolean shouldExit = false;

        while (!shouldExit) {
            String msg = scanner.nextLine();
            if (msg.equalsIgnoreCase("/logout")) {
                commandHandler.handleCommand("/logout", name);
                return;
            } else if (msg.startsWith("/")) {
                commandHandler.handleCommand(msg, name);
            } else {
                Pattern sendToPattern = Pattern.compile("^SEND TO:(.*)");

                Matcher sendToMatcher = sendToPattern.matcher(msg);
                if (sendToMatcher.find()) {
                    String targetUser = RegexPatterns.SEND_TO.matcher(msg).replaceFirst("$1").trim();
                    String fullMsg = "USER: " + targetUser + " " + name + ": " + msg.substring(("SEND TO:" + targetUser).length()).trim();
                    sendMessage(fullMsg);
                } else {
                    String fullMsg = name + ": " + msg;
                    sendMessage(fullMsg);
                }
            }
        }
    }

    private void sendMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, socket.getLocalPort());
        socket.send(packet);
    }
}