package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.Services.MulticastManager;
import org.estg.ipp.pt.Services.MulticastManagerService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Scanner;

public class MulticastChatService extends AbstractChatService {
    private final MulticastManagerService multicastManagerService;
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;

    public MulticastChatService(String groupAddress, int port, String name,
                                MulticastManagerService multicastManagerService,
                                CommandHandler commandHandler, MessageHandler messageHandler) throws IOException {
        super(groupAddress, port, name);
        this.multicastManagerService = multicastManagerService;
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
    }

    @Override
    public void startChat(String groupAddress, int port, String name) throws IOException {

        try (Socket serverSocket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true)) {
            out.println("READY:" + name);
            System.out.println("READY enviado ao servidor");
        } catch (IOException e) {
            System.err.println("Erro ao notificar servidor sobre READY: " + e.getMessage());
        }
        // Criar ou obter o MulticastManager
        MulticastManager multicastManager = multicastManagerService.getOrCreateMulticastManager(groupAddress, port);

        // Enviar uma mensagem de boas-vindas (opcional)
        multicastManager.sendMessage(name + " entrou no chat!");

        // Iniciar a recepção de mensagens em uma thread separada
        multicastManager.receiveMessages(messageHandler);  // Passa o MessageHandler para processar as mensagens recebidas

        // Iniciar o envio de mensagens
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite a mensagem (/logout para sair): ");
        boolean shouldExit = false;

        while (!shouldExit) {
            String msg = scanner.nextLine();
            if (msg.equalsIgnoreCase("/logout")) {
                commandHandler.handleCommand("/logout", name);
                shouldExit = true;
            } else if (msg.startsWith("/")) {
                commandHandler.handleCommand(msg, name);
            } else {
                // Enviar a mensagem para o grupo multicast
                System.out.println("Enviando mensagem: " + name + ": " + msg);
                multicastManager.sendMessage(name + ": " + msg);
            }
        }

        // Fechar o socket quando sair
        multicastManager.close();
    }
}
