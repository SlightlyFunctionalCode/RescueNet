package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;

import static java.lang.System.out;
import static org.estg.ipp.pt.Notifications.notifyUser;
import static org.estg.ipp.pt.Server.*;

@Component
public class ExecuteInternalCommands {

    @Autowired
    public UserService userService;

    @Autowired
    public GroupService groupService;

    public boolean isInternalCommand(String command) {
        return command.equals("REGISTER") || command.equals("LOGIN") || command.equals("LOGOUT");
    }

    public void handleInternalCommand(String command, String payload, PrintWriter out, Socket clientSocket, List<Group> groupList) {
        switch (command) {
            case "REGISTER" -> handleRegister(payload, out);
            case "LOGIN" -> handleLogin(payload, out, clientSocket, groupList);
            case "LOGOUT" -> handleLogout(payload, out);
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
            user.setPermissions(Permissions.LOW_LEVEL);

            try {
                // Registra o usuário no banco de dados
                int result = userService.register(user);

                if (result == 0) {
                    out.println("FAILED: Usuário com nome ou email já existente");
                    return; // Retorna sem associar ao grupo se o registro falhar
                }

                out.println("SUCESSO: Usuário registrado e associado ao grupo padrão com sucesso");

            } catch (Exception e) {
                out.println("ERRO: Falha ao registrar usuário - " + e.getMessage());
            }
        } else {
            out.println("ERRO: Formato inválido para REGISTER");
        }
    }

    private void handleLogin(String payload, PrintWriter out, Socket clientSocket, List<Group> groupList) {
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
            }else {
                groupService.addUserToGroup(user.getPermissions().toString(), user.getId());
            }

            String response = loginUser(usernameOrEmail, password, clientSocket, groupList);
            System.out.println(response);

            out.println(response);

            } else {
            out.println("ERRO: Formato inválido para LOGIN");
        }
    }

    private String loginUser(String usernameOrEmail, String password, Socket clientSocket, List<Group> groupList) {

        User user = userService.authenticate(usernameOrEmail, password);

        if (user == null) {
            return "FAILED: Usuário inválido!";
        }

        // Após login bem-sucedido, armazenar o socket e verificar permissões
        userSockets.put(usernameOrEmail, clientSocket);

        if (user.getPermissions() == Permissions.HIGH_LEVEL || user.getPermissions() == Permissions.MEDIUM_LEVEL) {
            usersWithPermissionsOnline.add(usernameOrEmail);
            // Enviar notificações para pedidos pendentes
            for (Map.Entry<String, String> entry : pendingApprovals.entrySet()) {
                String requestingUser = entry.getKey();
                String operationName = entry.getValue();
                notifyUser(usernameOrEmail, "Pedido pendente: O usuário " + requestingUser + " solicitou a operação '" + operationName + "'. Aprove ou rejeite.", usersWithPermissionsOnline, groupList);
            }
        }
        if (user.getGroups().isEmpty()) {
            throw new IllegalArgumentException("O usuário não está associado a nenhum grupo");
        }

        Group group = groupService.getUserGroupByName(user.getId(), user.getGroups().get(0).getName());

        return "SUCESSO: Login realizado. Grupo: " + group.getAddress() + ":" + group.getPort();
    }

    public static Socket getUserSocket(String username) {
        Socket socket = userSockets.get(username);
        if (socket == null) {
            out.println("Erro: Socket do utilizador " + username + " não encontrado.");
        }
        return socket;
    }


    private void handleLogout(String username, PrintWriter out) {
        if (userSockets.containsKey(username)) {
            userSockets.remove(username);
            out.println("SUCESSO: Logout realizado");
        } else {
            out.println("ERRO: Usuário não está logado");
        }
    }

}


