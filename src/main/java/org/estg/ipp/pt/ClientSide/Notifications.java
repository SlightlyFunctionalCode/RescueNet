package org.estg.ipp.pt.ClientSide;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;
import org.estg.ipp.pt.Services.MulticastManagerService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import static org.estg.ipp.pt.Server.getUserSocket;


public class Notifications {

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

    public static void notifyUser(String username, String message,
                                  Set<String> usersWithPermissionsOnline,
                                  Map<String, String> pendingApprovals) {
        // Verificar se o usuário está online
        if (!usersWithPermissionsOnline.contains(username)) {
            System.out.println("Utilizador " + username + " não está online. Registrando notificação pendente.");
            saveNotificationForLater(username, message, pendingApprovals);
            return;
        }

        // Obter o socket associado ao utilizador
        Socket socket = getUserSocket(username);
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket indisponível para " + username + ". Registrando notificação pendente.");
            saveNotificationForLater(username, message, pendingApprovals);
            return;
        }

        // Tentar enviar a mensagem via socket
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            System.out.println("Notificação enviada para o utilizador " + username + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar notificação para " + username + ": " + e.getMessage());
            saveNotificationForLater(username, message, pendingApprovals);
        }
    }

    /**
     * Salva a notificação para ser entregue posteriormente.
     */
    private static void saveNotificationForLater(String username, String message,
                                                 Map<String, String> pendingApprovals) {
        // Adiciona a notificação ao map ou log para entrega posterior
        pendingApprovals.put(username, message);
        System.out.println("Notificação salva para entrega posterior: " + message + " para " + username);
    }

    protected static void sendMessageToUser(String username, String message, Set<String> usersWithPermissionsOnline) {
        // Verifica se o usuário está online e tem um socket ativo
        if (usersWithPermissionsOnline.contains(username)) {
            try {
                Socket userSocket = getUserSocket(username);
                if (userSocket != null) {
                    PrintWriter userOut = new PrintWriter(userSocket.getOutputStream(), true);
                    userOut.println(message);  // Envia a mensagem para o usuário
                    System.out.println("Mensagem enviada para " + username + ": " + message);
                } else {
                    System.out.println("Erro: Socket do usuário " + username + " não encontrado.");
                }
            } catch (IOException e) {
                System.out.println("Erro ao enviar mensagem para o usuário " + username + ": " + e.getMessage());
            }
        } else {
            System.out.println("Usuário " + username + " não está online.");
        }
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
