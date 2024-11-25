package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class DefaultMessageHandler extends AbstractMessageHandler {
    private MulticastChatService chatService;

    public DefaultMessageHandler(MulticastChatService chatService) {
        super();
        this.chatService = chatService;
    }

    public MulticastChatService getChatService() {
        return chatService;
    }

    public void setChatService(MulticastChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected void handleChatRequest(String message) {
        String[] parts = message.split(":");
        String address = parts[1];
        String port = parts[2];

        System.out.println("Chat request received from: " + address + ":" + port);
        System.out.print("Type 'yes' to accept or 'no' to decline: ");

        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("yes")) {
            try {
                startPrivateChat(address, port, chatService.name);
            } catch (IOException e) {
                System.err.println("Erro ao iniciar chat privado: " + e.getMessage());
            }
        } else {
            System.out.println("Chat request declined.");
        }
    }

    protected void startPrivateChat(String address, String port, String name) throws IOException {
        int _port;
        try {
            _port = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number.");
            return;
        }

        try (Socket privateSocket = new Socket(address, _port);
             BufferedReader privateIn = new BufferedReader(new InputStreamReader(privateSocket.getInputStream()));
             PrintWriter privateOut = new PrintWriter(privateSocket.getOutputStream(), true)) {

            System.out.println("Private chat started with " + address + ":" + _port);

            Thread receiverThread = new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = privateIn.readLine()) != null) {
                        System.out.println("Private message: " + incomingMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Private chat closed.");
                }
            });
            receiverThread.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Your message (type 'exit' to end): ");
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                privateOut.println(name + ": " + message);
            }

            System.out.println("Exiting private chat...");
        } catch (IOException e) {
            System.err.println("Error in private chat with " + address + ":" + port + ": " + e.getMessage());
        }
    }
}
