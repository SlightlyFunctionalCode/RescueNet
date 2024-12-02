package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Services.GroupService;
import org.estg.ipp.pt.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;

import static java.lang.System.out;
import static org.estg.ipp.pt.ClientSide.Notifications.notifyGroup;
import static org.estg.ipp.pt.Server.userSockets;

@Component
public class ExecuteInternalCommands {

    @Autowired
    public UserService userService;

    @Autowired
    public GroupService groupService;

    public boolean isInternalCommand(String command) {
        return command.equals("REGISTER") || command.equals("LOGIN") || command.equals("LOGOUT") || command.equals("READY");
    }

    public void handleInternalCommand(String command, String payload, PrintWriter out, Socket clientSocket, List<Group> groupList, Set<String> usersWithPermissionsOnline, Map<String, String> pendingApprovals) {
        switch (command) {
            case "REGISTER" -> handleRegister(payload, out);
            case "LOGIN" -> handleLogin(payload, out, clientSocket, groupList, usersWithPermissionsOnline, pendingApprovals);
            case "LOGOUT" -> handleLogout(payload, out);
            case "READY" -> handlePenddingRequest(payload, out, pendingApprovals);
            default -> out.println("ERRO: Comando interno inválido");
        }
    }

    private void handleRegister(String payload, PrintWriter out) {
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
                // Registra o usuário no banco de dados
                int result = userService.register(user);

                if (result == 0) {
                    out.println("FAILED: Utilizador com nome ou email já existente");
                    return; // Retorna sem associar ao grupo se o registro falhar
                }
                System.out.println("Adicionando user ao grupo default");
                List<Group> groups = groupService.getAllGroups();
                for (Group group : groups) {
                    if(group.isPublic() && Permissions.fromPermissions(user.getPermissions()) > Permissions.fromPermissions(group.getRequiredPermissions())){
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

    private void handleLogin(String payload, PrintWriter out, Socket clientSocket, List<Group> groupList, Set<String> usersWithPermissionsOnline, Map<String, String> pendingApprovals) {
        Matcher loginMatcher = RegexPatterns.LOGIN.matcher(payload);
        System.out.println("loginMatcher: " + loginMatcher);

        if (loginMatcher.matches()) {
            String usernameOrEmail = loginMatcher.group("username");
            String password = loginMatcher.group("password");
            System.out.println("usernameOrEmail: " + usernameOrEmail + ", password: " + password);

            User user = userService.getUserByName(usernameOrEmail);

            // Após salvar o usuário, associa-o a um grupo padrão
            if(user == null){
                out.println("User inválido");
            }

            String response = loginUser(usernameOrEmail, password, clientSocket, groupList,
                    usersWithPermissionsOnline);
            System.out.println(response);


            /*TODO: Devo guardar junto a permissao*/
            if (user.getPermissions() == Permissions.HIGH_LEVEL || user.getPermissions() == Permissions.MEDIUM_LEVEL || user.getPermissions() == Permissions.LOW_LEVEL) {
                usersWithPermissionsOnline.add(user.getName());
                System.out.println("User com permissões deu join");
            }
            out.println(response);

            } else {
            out.println("ERRO: Formato inválido para LOGIN");
        }
    }

    private String loginUser(String usernameOrEmail, String password, Socket clientSocket, List<Group> groupList, Set<String> usersWithPermissionsOnline) {

        User user = userService.authenticate(usernameOrEmail, password);

        if (user == null) {
            return "FAILED: Usuário inválido!";
        }
        String username = user.getName();
        // Após login bem-sucedido, armazenar o socket e verificar permissões
        userSockets.put(username, clientSocket);

        Group group = groupService.getGroupByName("GERAL");

        try {
            userService.joinGroup(user, group);
        } catch (IllegalArgumentException e) {
        }
        return "SUCESSO: Login realizado. Grupo: " + group.getAddress() + ":" + group.getPort();
    }

    private void handleLogout(String username, PrintWriter out) {
        if (userSockets.containsKey(username)) {
            userSockets.remove(username);
            out.println("SUCESSO: Logout realizado");
        } else {
            out.println("ERRO: Usuário não está logado");
        }
    }

    private void handlePenddingRequest(String payload, PrintWriter out, Map<String, String> pendingApprovals) {
        Matcher registerMatcher = RegexPatterns.READY.matcher(payload);
        if (registerMatcher.matches()) {
            String username = registerMatcher.group("username");
            User user = userService.getUserByName(username);
            if (user.getPermissions() == Permissions.HIGH_LEVEL || user.getPermissions() == Permissions.MEDIUM_LEVEL) {
                // Enviar notificações para pedidos pendentes
                for (Map.Entry<String, String> entry : pendingApprovals.entrySet()) {
                    String requestingUser = entry.getKey();
                    String operationName = entry.getValue();
                    System.out.println(user.getCurrentGroup().getAddress());
                    System.out.println(user.getCurrentGroup().getPort());
                    notifyGroup(user.getCurrentGroup(), "Pedido pendente: O usuário " + requestingUser + " solicitou a operação '" + operationName + "'. Aprove ou rejeite.");
                }
            }
        }
    }

}


