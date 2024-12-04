package org.estg.ipp.pt.ServerSide.Services;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Server;
import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import static org.estg.ipp.pt.Server.getUserSocket;

public class NotificationHandler {

    public static void sendNotificationToGroup(String message, Group group) {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();

            // Iterar sobre todos os grupos de multicast
            InetAddress groupAddress = InetAddress.getByName(group.getAddress());
            byte[] msg = message.getBytes();

            DatagramPacket packet = new DatagramPacket(msg, msg.length, groupAddress, group.getPort());
            socket.send(packet);

            System.out.println("Enviando notificação para " + group.getAddress() + ":" + group.getPort() + " - " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        socket.close();
    }

    public static void notify(String username, String message) {

        // Obter o socket associado ao utilizador
        Socket socket = getUserSocket(username);

        if (socket == null || socket.isClosed()) {
            System.out.println("Socket indisponível para " + username + ". Registrando notificação pendente.");
            return;
        }
        // Tentar enviar a mensagem via socket
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            System.out.println("Notificação enviada para o utilizador " + username + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar notificação para " + username + ": " + e.getMessage());
        }
    }

    public static void sendMessage(String targetUsername, Message message) {
        Socket targetSocket = Server.getUserSocket(targetUsername);

        if (targetSocket != null) {
            try {
                PrintWriter targetOutput = new PrintWriter(targetSocket.getOutputStream(), true);
                targetOutput.println(message.getContent());
                System.out.printf("Envio de %s para o utilizador %s%n", message.getContent(), targetUsername);
            } catch (IOException e) {
                System.out.printf("Error sending message to %s%n", targetUsername);
            }
        } else {
            System.out.printf("Utilizador %s offline. A guardar mensagem para quando o utilizador fizer login%n", targetUsername);
        }
    }

    public static void notifyApprovalRequest(String username, String message,
                                  Set<String> usersWithPermissionsOnline, MessageService messageService) {
        // Verificar se o usuário está online
        if (!usersWithPermissionsOnline.contains(username)) {
            System.out.println("Utilizador " + username + " não está online. Registrando notificação pendente.");
            saveNotificationForLater(username, message, messageService);
            return;
        }

        // Obter o socket associado ao utilizador
        Socket socket = getUserSocket(username);
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket indisponível para " + username + ". Registrando notificação pendente.");
            saveNotificationForLater(username, message, messageService);
            return;
        }

        // Tentar enviar a mensagem via socket
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            System.out.println("Notificação enviada para o utilizador " + username + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar notificação para " + username + ": " + e.getMessage());
            if (!message.startsWith("SUCESSO")) {
                saveNotificationForLater(username, message, messageService);
            }
        }
    }

    /**
     * Salva a notificação para ser entregue posteriormente.
     */
    private static void saveNotificationForLater(String username, String message, MessageService messageService) {
        Message approval = new Message(username, "null", message, true);

        messageService.saveMessage(approval);

        System.out.println("Notificação salva para entrega posterior: " + message + " para " + username);
    }

    public static void notifyGroup(Group notifyGroup, String message) {
        try {
            // Obtém a instância do MulticastManagerService
            MulticastManagerService service = MulticastManagerService.getInstance();

            // Obtém ou cria o MulticastManager associado ao grupo
            MulticastManager manager = service.getOrCreateMulticastManager(
                    notifyGroup.getAddress(),
                    notifyGroup.getPort()
            );

            // Envia a mensagem usando o MulticastManager
            manager.sendMessage(message);

        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }
}
