package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Server;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessInternalCommands;
import org.estg.ipp.pt.ServerSide.Services.GroupService;
import org.estg.ipp.pt.ServerSide.Services.MessageService;
import org.estg.ipp.pt.ServerSide.Services.NotificationHandler;
import org.estg.ipp.pt.ServerSide.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static org.estg.ipp.pt.ServerSide.Services.NotificationHandler.notifyGroup;

@Component
public class ProcessInternalCommandsImpl implements ProcessInternalCommands {
    @Autowired
    public UserService userService;

    @Autowired
    public GroupService groupService;

    @Autowired
    private MessageService messageService;

    public void handleIsReadConfirmation(String payload) {
        try {
            Matcher confirmMatcher = RegexPatterns.CONFIRM_READ.matcher(payload.trim());
            if (confirmMatcher.matches()) {
                String messageId = confirmMatcher.group("id");

                if (messageId == null) {
                    System.err.println("Failed to mark message as read");
                    return;
                }

                // Parse and mark as read
                long messageIdLong = Long.parseLong(messageId);
                messageService.markAsRead(messageIdLong);
                System.out.println("Message " + messageId + " marked as read by Thread: " + Thread.currentThread().getName());
            } else {
                System.err.println("Invalid CONFIRM_READ payload: " + payload);
            }
        } catch (Exception e) {
            System.err.println("Error processing CONFIRM_READ: " + e.getMessage());
        }
    }


    public void handleRegister(String payload, PrintWriter out) {
        Matcher registerMatcher = RegexPatterns.REGISTER.matcher(payload);
        if (registerMatcher.matches()) {
            String username = registerMatcher.group("username");
            String email = registerMatcher.group("email");
            String password = registerMatcher.group("password");

            User user = new User();
            user.setName(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setPermissions(Permissions.NO_LEVEL);

            try {
                int result = userService.register(user);

                if (result == 0) {
                    out.println("FAILED: Utilizador com nome ou email já existente");
                    return;
                }
                System.out.println("Adicionando user ao grupo default");
                List<Group> groups = groupService.getAllGroups();
                for (Group group : groups) {
                    if (group.isPublic() && Permissions.fromPermissions(user.getPermissions()) > Permissions.fromPermissions(group.getRequiredPermissions())) {
                        groupService.addUserToGroup(group.getName(), user);
                    }
                }

                out.println("SUCESSO: Utilizador registrado com sucesso");
            } catch (Exception e) {
                out.println("ERRO: Falha ao registrar utilizador - " + e.getMessage());
            }
        } else {
            out.println("ERRO: Formato inválido para REGISTER ");
        }
    }

    public void handleLogin(String payload, PrintWriter out, Socket clientSocket, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) {
        if (payload == null || payload.isEmpty()) {
            out.println("Erro: Dados de login inválidos.");
            return;
        }

        Matcher loginMatcher = RegexPatterns.LOGIN.matcher(payload);
        if (loginMatcher.matches()) {
            String usernameOrEmail = loginMatcher.group("username");
            String password = loginMatcher.group("password");
            System.out.println("usernameOrEmail: " + usernameOrEmail + ", password: " + password);

            User user = userService.getUserByNameOrEmail(usernameOrEmail);

            if (user == null) {
                out.println("User inválido");
                return;
            }

            String response = loginUser(user, password, clientSocket);
            System.out.println(response);

            if (response.startsWith("SUCESSO:") && user.getPermissions() == Permissions.HIGH_LEVEL
                    || user.getPermissions() == Permissions.MEDIUM_LEVEL
                    || user.getPermissions() == Permissions.LOW_LEVEL) {
                usersWithPermissionsOnline.put(user.getName(), user.getPermissions());
                System.out.println("User com permissões deu join");
            }

            out.println(response);
        } else {
            out.println("ERRO: Formato inválido para LOGIN");
        }
    }

    private void sendUnreadChatMessage(String username) {
        List<Message> unreadMessages = messageService.getUnreadMessages(username);

        for (Message unreadMessage : unreadMessages) {

            NotificationHandler.sendMessage(unreadMessage.getReceiver(), unreadMessage);
        }
    }

    private void sendLatestMulticastMessages(String username) {
        User user = userService.getUserByName(username);

        if (user == null) {
            System.out.println("ERRO: Ocorrer um erro ao repor as mensagens");
            return;
        }

        List<Message> unreadMessages = messageService.getLastestGroupMessages(user.getCurrentGroup().getName());

        for (Message unreadMessage : unreadMessages) {
            unreadMessage.setContent(unreadMessage.getSender() + ":" + unreadMessage.getContent());
            NotificationHandler.sendMessage(user.getName(), unreadMessage);
        }
    }

    private String loginUser(User user, String password, Socket clientSocket) {

        boolean isValid = userService.authenticate(user, password);

        if (!isValid) {
            return "FAILED: Utilizador inválido!";
        }
        String username = user.getName();

        if (Server.getUserSocket(username) != null) {
            return "FAILED: Utilizador já está logado!";
        }

        Server.addUserSocket(username, clientSocket);

        Group group = groupService.getGroupByName("GERAL");

        try {
            userService.joinGroup(user, group);
        } catch (IllegalArgumentException e) {
        }
        return "SUCESSO: Login realizado. Grupo: " + group.getAddress() + ":" + group.getPort() + ":" + username;
    }

    public void handleLogout(String username, PrintWriter out) {
        if (Server.getUserSocket(username) != null) {
            Server.removeUserSocket(username);
            out.println("SUCESSO: Logout realizado");
        } else {
            out.println("ERRO: Utilizador não está logado");
        }
    }

    public void handlePendingRequest(String payload) {
        Matcher registerMatcher = RegexPatterns.READY.matcher(payload);
        if (registerMatcher.matches()) {
            String username = registerMatcher.group("username");

            sendUnreadChatMessage(username);
            sendLatestMulticastMessages(username);

            User user = userService.getUserByName(username);
            if (user.getPermissions() == Permissions.HIGH_LEVEL || user.getPermissions() == Permissions.MEDIUM_LEVEL) {
                List<Message> pendingApprovals = messageService.getPendingApprovalRequests();

                for (Message m : pendingApprovals) {
                    String requestingUser = m.getSender();
                    String operationName = m.getContent();

                    notifyGroup(groupService.getGroupByName("HIGH_LEVEL"), "Pedido pendente: O utilizador " + requestingUser + " solicitou a operação '" + operationName + "'" + " com o id " + m.getId() + ". Aprove ou rejeite.");
                }
            }
        }
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public UserService getUserService() {
        return userService;
    }
}
