package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Services.GroupService;
import org.estg.ipp.pt.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

import static org.estg.ipp.pt.Server.*;
import static org.estg.ipp.pt.Services.ExecuteInternalCommands.getUserSocket;
import static org.estg.ipp.pt.Services.ExecuteUserCommands.saveNotificationForLater;


public class Notifications {

    public static void sendNotificationToGroups(String message, List<Group> multicastGroups) {
        try {
            DatagramSocket socket = new DatagramSocket();

            // Iterar sobre todos os grupos de multicast
            for (Group group : multicastGroups) {
                InetAddress groupAddress = InetAddress.getByName(group.getAddress());
                byte[] msg = message.getBytes();

                DatagramPacket packet = new DatagramPacket(msg, msg.length, groupAddress, group.getPort());
                socket.send(packet);

                System.out.println("Enviando notificação para " + group.getAddress() + ":" + group.getPort() + " - " + message);
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao enviar notificação: " + e.getMessage());
        }
    }

    protected static void sendNotificationToGroupHIGH_LEVEL(String message, Group group) {
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

    protected static void sendNotificationToGroupMEDIUM_LEVEL(String message, AbstractMap.SimpleEntry<String, Integer> group) {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();

            // Iterar sobre todos os grupos de multicast
            InetAddress groupAddress = InetAddress.getByName(group.getKey());
            byte[] msg = message.getBytes();

            DatagramPacket packet = new DatagramPacket(msg, msg.length, groupAddress, group.getValue());
            socket.send(packet);

            System.out.println("Enviando notificação para " + group.getKey() + ":" + group.getValue() + " - " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        socket.close();
    }

    public static void notifyUser(String username, String message, Set<String> usersWithPermissionsOnline,
                                  Group group, Map<String, String> pendingApprovals) {
        // Log para debug
        System.out.println("Tentando notificar " + username + ": " + message);

        // Verifica se o usuário está na lista de usuários online com permissões
        if (usersWithPermissionsOnline.contains(username)) {
            // O usuário está online, agora vamos notificar
            System.out.println("Notificação enviada para o utilizador " + username + ": " + message);

            // Enviar a mensagem para o chat ou algum outro mecanismo de comunicação
            // Supondo que você tenha algum serviço de mensagens, como um chat
            sendNotificationToGroupHIGH_LEVEL(message, group);
        } else {
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

    public static void sendNotificationToUserInGroup(String username, String message, Set<String> usersWithPermissionsOnline, UserService userService, GroupService groupService) {
        // Verifica se o usuário está online
        if (!usersWithPermissionsOnline.contains(username)) {
            System.out.println("Usuário " + username + " não está online ou não tem permissões.");
            return;
        }

        try {
            // Formata a mensagem para ser enviada para um usuário específico
            String fullMessage = "USER: " + username + " " + message; // Prefixa com o nome do destinatário

            User user = userService.getUserByName(username);

            Group group_user = new Group();
            groupService.getUserGroupByNameAndVerify(user.getId(), user.getCurrentGroup().getName());
            String groupAddress = group_user.getAddress();
            int port = group_user.getPort();

            // Criando o socket multicast para enviar a mensagem para o grupo
            InetAddress group = InetAddress.getByName(groupAddress);

            MulticastSocket socket = new MulticastSocket();
            socket.joinGroup(group);

            byte[] buffer = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);

            socket.send(packet);
            socket.leaveGroup(group);
            socket.close();

            System.out.println("Mensagem enviada para o usuário " + username + " no grupo " + group.toString() + ":" + port + " - " + message);
        } catch (IOException e) {
            System.out.println("Erro ao enviar mensagem para o usuário " + username + ": " + e.getMessage());
        }
    }
}
