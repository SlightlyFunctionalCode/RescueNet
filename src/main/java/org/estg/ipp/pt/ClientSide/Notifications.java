package org.estg.ipp.pt.ClientSide;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.ServerSide.Managers.MulticastManager;
import org.estg.ipp.pt.Services.MulticastManagerService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import static org.estg.ipp.pt.ServerSide.Classes.Server.*;
import static org.estg.ipp.pt.ServerSide.Classes.ExecuteInternalCommands.getUserSocket;
import static org.estg.ipp.pt.ServerSide.Classes.ExecuteUserCommands.saveNotificationForLater;


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

    public static void notifyUser(String username, String message, Set<String> usersWithPermissionsOnline,
                                   Map<String, String> pendingApprovals) {
        // Log para debug
        Socket socket = userSockets.get(username);
        if (socket == null || socket.isClosed()) {
            System.out.println("Tentando notificar " + username + ", mas o socket está indisponível.");
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message); // Enviar mensagem para o cliente
            System.out.println("Notificação enviada para o utilizador " + username + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar notificação para " + username + ": " + e.getMessage());
        }
        if (usersWithPermissionsOnline.contains(username)) {
            // Se o usuário não está online, você pode decidir se ainda assim quer registrar a notificação
            System.out.println("Utilizador não está online, aguardando conexão...");
            // Aqui você pode salvar a notificação em algum local de espera ou log, se necessário
            // Exemplo:
            saveNotificationForLater(username, message, pendingApprovals);
        }
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
