package org.estg.ipp.pt.ServerSide.Classes;

import jakarta.persistence.EntityNotFoundException;
import org.estg.ipp.pt.Classes.Enum.*;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.ServerSide.Services.NotificationHandler;
import org.estg.ipp.pt.ServerSide.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static org.estg.ipp.pt.Classes.Interfaces.HelpMessageInterface.*;
import static org.estg.ipp.pt.ServerSide.Services.NotificationHandler.*;

@Component
public class ExecuteUserCommands {
    @Autowired
    private UserService userService;
    @Autowired
    private LogService logService;
    @Autowired
    public GroupService groupService;
    @Autowired
    public MessageService messageService;

    public void handleUserCommand(String command, String request, String requester, String payload, PrintWriter out,
                                  Set<String> usersWithPermissionsOnline) throws IOException {

        /*TODO: Adicionar Comando para adicionar pessoas aos grupos personalizados*/
        /*TODO: Adicionar comando para listar grupos que um user pode dar join */
        /*TODO: Adicionar comando para listar todos os comandos disponíveis */
        switch (command) {
            case "/evac", "/resdist", "/emerg" ->
                    processOperationCommand(payload, command, out, usersWithPermissionsOnline);
            case "/approve" -> {
                Matcher approveMatcher = RegexPatternsCommands.APPROVE.matcher(request);
                if (approveMatcher.matches()) {
                    String help = approveMatcher.group("help");

                    String username;
                    if (help != null) {
                        out.println(APPROVE_HELP);
                    } else if (requester != null) {
                        long id = 0;
                        try {
                            id = Long.parseLong(approveMatcher.group("id"));
                        } catch (NumberFormatException e) {
                            out.println("ERRO: Formato inválido para APPROVE. Use -h para descobrir os parâmetros");
                            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para APPROVE"));
                        }

                        requester = approveMatcher.group("requester");
                        username = approveMatcher.group("username");
                        handleApprovalCommand(command, id, username, requester, out);
                    } else {
                        out.println("ERRO: Formato inválido para APPROVE. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para APPROVE"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para APPROVE. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para APPROVE"));
                }
            }
            case "/reject" -> {
                Matcher rejectMatcher = RegexPatternsCommands.REJECT.matcher(request);
                if (rejectMatcher.matches()) {
                    String help = rejectMatcher.group("help");
                    String username;


                    if (help != null) {
                        out.println(REJECT_HELP);
                    } else if (requester != null) {
                        long id = 0;
                        try {
                            id = Long.parseLong(rejectMatcher.group("id"));
                        } catch (NumberFormatException e) {
                            out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                        }

                        requester = rejectMatcher.group("requester");
                        username = rejectMatcher.group("username");
                        handleApprovalCommand(command, id, username, requester, out);
                    } else {
                        out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                }
            }
            case "/export" -> processExport(request, out);
            case "/join" -> {
                Matcher joinMatcher = RegexPatternsCommands.JOIN.matcher(request);
                if (joinMatcher.matches()) {
                    String help = joinMatcher.group("help");
                    String name = joinMatcher.group("name");

                    if (help != null) {
                        out.println(JOIN_HELP);
                    } else if (name != null) {
                        processJoinCommand(payload, name, out);
                    } else {
                        out.println("ERRO: Formato inválido para /join. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /join"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para /join. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /join"));
                }
            }
            case "/change_permission" -> {
                Matcher changPermissionMatcher = RegexPatternsCommands.CHANGE_PERMISSIONS.matcher(request);
                if (changPermissionMatcher.matches()) {
                    String help = changPermissionMatcher.group("help");
                    String name = changPermissionMatcher.group("name");
                    String perm = changPermissionMatcher.group("permission");

                    if (help != null) {
                        out.println(CHANGE_PERMISSION_HELP);
                    } else if (name != null && perm != null) {
                        try {
                            int permission = Integer.parseInt(perm);
                            Permissions permissions = Permissions.fromValue(permission);
                            processChangePermissionCommand(payload, name, permissions, out);
                        } catch (NumberFormatException e) {
                            out.println("ERRO: Formato inválido para /change_permission. Use -h para descobrir os parâmetros");
                            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /join"));
                        }
                    } else {
                        out.println("ERRO: Formato inválido para /change_permission. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /change_permission"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para /change_permission. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /change_permission"));
                }
            }
            case "/create_group" -> {
                Matcher createGroupMatcher = RegexPatternsCommands.CREATE_GROUP.matcher(request);
                if (createGroupMatcher.matches()) {
                    String help = createGroupMatcher.group("help");
                    String name = createGroupMatcher.group("name");
                    String publicOrPrivate = createGroupMatcher.group("publicOrPrivate");

                    if (help != null) {
                        out.println(CREATE_GROUP_HELP);
                    } else if (name != null && publicOrPrivate != null) {
                        processCreateGroupCommand(payload, name, publicOrPrivate, out);
                    } else {
                        out.println("ERRO: Formato inválido para /create_group. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /create_group"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para /create_group. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /create_group"));
                }
            }
            case "/chat" -> {
                Matcher chatMatcher = RegexPatternsCommands.CHAT.matcher(request);
                if (chatMatcher.matches()) {
                    String targetUsername = chatMatcher.group("targetUsername");
                    String message = chatMatcher.group("message");
                    String username = chatMatcher.group("username");
                    if (targetUsername != null && !targetUsername.isEmpty() && message != null && !message.isEmpty()) {
                        Long id = messageService.saveMessage(new Message(username, targetUsername, ""));

                        String content = String.format("PRIVATE:/%s/ %s: %s", id.toString(), username, message);

                        Message createdMessage;
                        try {
                            createdMessage = messageService.updateContent(content, id);

                            NotificationHandler.sendMessage(targetUsername, createdMessage);
                            out.println("Mensagem enviada com sucesso.");
                        } catch (IllegalArgumentException e) {
                            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /chat"));
                            out.println("ERRO: Por favor, forneça o nome de utilizador do destinatário e a mensagem. Use -h para ajuda.");
                        }
                    } else {
                        out.println("ERRO: Por favor, forneça o nome de utilizador do destinatário e a mensagem. Use -h para ajuda.");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /chat"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para /chat. Use -h para ajuda.");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /chat"));
                }
            }
            case "/commands" -> {
                System.out.println(request);
                Matcher commandsHelper = RegexPatternsCommands.COMMANDS.matcher(request);
                if (commandsHelper.matches()) {
                    out.println("COMMANDS START");
                    String name = commandsHelper.group("name");
                    handleCommandHelper(name);
                } else {
                    out.println("ERRO: Formato inválido para /create_group. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /create_group"));
                }
            }
        }
    }

    private void processExport(String request, PrintWriter out) {
        Matcher exportMatcher = RegexPatternsCommands.EXPORT.matcher(request);

        if (exportMatcher.matches()) { // Check if the matcher found a match
            String help = exportMatcher.group("help");
            String startDateString = exportMatcher.group("startDate");
            String endDateString = exportMatcher.group("endDate");
            String tagString = exportMatcher.group("tag");
            String username = exportMatcher.group("username");

            try {
                if (help != null) {
                    out.println(EXPORT_HELP);
                } else if (startDateString != null && endDateString != null && tagString != null) {
                    LocalDateTime startDate = LocalDateTime.parse(startDateString);
                    LocalDateTime endDate = LocalDateTime.parse(endDateString);
                    TagType tag = TagType.valueOf(tagString);

                    processExportByDateRangeAndTagCommand(startDate, endDate, tag, username, out);
                } else if (tagString != null) {
                    TagType tag = TagType.valueOf(tagString);

                    processExportByTagCommand(tag, username, out);
                } else if (startDateString != null && endDateString != null) {
                    LocalDateTime startDate = LocalDateTime.parse(startDateString);
                    LocalDateTime endDate = LocalDateTime.parse(endDateString);

                    processExportByDateRangeCommand(startDate, endDate, username, out);
                } else {
                    out.println("ERRO: Formato inválido para /export. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /export."));
                }
            } catch (DateTimeParseException | IllegalArgumentException ex) {
                out.println("ERRO: Formato inválido para /export. Use -h para descobrir os parâmetros");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /export."));
            }
        } else {
            out.println("ERRO: Formato inválido para /export. Use -h para descobrir os parâmetros");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /export."));
        }
    }

    private void processExportByDateRangeCommand(LocalDateTime startDate, LocalDateTime endDate, String username, PrintWriter out) {
        // Generate the endpoint URL
        String url = "http://localhost:8080/download-pdf-report?startDate=" + startDate + "&endDate=" + endDate;

        processExportURL(url, username, out);
    }

    private void processExportByDateRangeAndTagCommand(LocalDateTime startDate, LocalDateTime endDate, TagType tagType, String username, PrintWriter out) {
        // Generate the endpoint URL
        String url = "http://localhost:8080/download-pdf-report?startDate=" + startDate + "&endDate=" + endDate + "&tag=" + tagType.name();

        processExportURL(url, username, out);
    }

    private void processExportByTagCommand(TagType tagType, String username, PrintWriter out) {
        // Generate the endpoint URL
        String url = "http://localhost:8080/download-pdf-report?tag=" + tagType.name();

        processExportURL(url, username, out);
    }

    private void processExportURL(String url, String username, PrintWriter out) {
        // Log the generated URL
        System.out.println("Generated URL for download: " + url);

        // Notify the client to download the file
        out.println("SUCESSO: O pdf foi gerado com sucesso. Por favor, faça o download aqui: " + url);

        // Save log for success
        logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "O pdf gerado por " + username + " foi gerado com sucesso"));
    }

    private void processJoinCommand(String username, String name, PrintWriter out) {
        // Buscar o usuário com o nome fornecido
        User user = userService.getUserByName(username); // Método para encontrar o usuário pelo nome de usuário
        if (user == null) {
            out.println("ERRO: Usuário não encontrado");
            return;
        }
        // Buscar o grupo com os parâmetros fornecidos
        Group group = groupService.getGroupByName(name);// Método para buscar o grupo
        if (group == null) {
            out.println("ERRO: Grupo não encontrado");
            return;
        }
        if (group.isPublic() && Permissions.fromPermissions(group.getRequiredPermissions()) < Permissions.fromPermissions(user.getPermissions())) {
            System.out.println("SUCESSO: Usuário " + username + " entrou no grupo " + name);
            try {
                userService.joinGroup(user, group);
            } catch (IllegalArgumentException e) {
                out.println("ERRO: " + e.getMessage());
                return;
            }
            // Agora, permitir que o usuário entre no chat
            String connectionInfo = group.getAddress() + ":" + group.getPort();
            out.println("CHAT_GROUP:" + connectionInfo);
        } else {
            out.println("Grupo é privado ou não tem permissões para dar join nesse grupo");
        }
    }

    private void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out) {
        User userWithPermissions = userService.getUserByName(username); // Método para encontrar o usuário pelo nome de usuário
        if (userWithPermissions == null) {
            out.println("ERRO: Usuário não encontrado");
            return;
        }
        User userUpdated = userService.getUserByName(name);
        List<Group> groups = groupService.getAllGroups();
        System.out.println(userWithPermissions.getPermissions());
        userService.updateUserPermissions(name, permission);
        if (Permissions.fromPermissions(userWithPermissions.getPermissions()) >= Permissions.fromPermissions(Permissions.HIGH_LEVEL)) {
            groupService.removeUserFromGroup(userUpdated, permission);
            out.println("SUCESSO: Usuário " + name + " promovido para " + permission.name());
        } else {

            for (Group group : groups) {
                if (group.isPublic() && Permissions.fromPermissions(userUpdated.getPermissions()) > Permissions.fromPermissions(group.getRequiredPermissions())) {
                    groupService.addUserToGroup(group.getName(), userUpdated);
                }
            }
            out.println("SUCESSO: Usuário " + name + " promovido para " + permission.name());
        }
    }

    private void processCreateGroupCommand(String username, String name, String publicOrPrivate, PrintWriter out) {
        User userWithPermissions = userService.getUserByName(username); // Método para encontrar o usuário pelo nome de usuário
        if (userWithPermissions == null) {
            out.println("ERRO: Usuário não encontrado");
            return;
        }

        if (!publicOrPrivate.equalsIgnoreCase("public") && !publicOrPrivate.equalsIgnoreCase("private")) {
            out.println("Verifique se o tipo de grupo está public ou private");
            return;
        }

        try {
            Group newGroup = groupService.addCustomGroup(userWithPermissions.getId(), name, publicOrPrivate);
            if (newGroup == null) {
                out.println("ERRO: Grupo não pode ser criado");
            } else {
                groupService.addUserToGroup(newGroup.getName(), userWithPermissions);
                out.println("SUCESSO: Grupo " + name + " criado com sucesso");
            }
        } catch (Exception e) {
            out.println("ERRO: " + e.getMessage());
        }
    }

    private void processOperationCommand(String username, String command, PrintWriter out,
                                         Set<String> usersWithPermissionsOnline) {
        User user = userService.getUserByName(username);
        if (user == null) {
            System.out.println("ERRO: Utilizador não encontrado.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Utilizador não encontrado."));
            return;
        }

        Operation operation = switch (command) {
            case "/evac" -> new Operation("Operação de evacuação em massa", Permissions.HIGH_LEVEL);
            case "/resdist" -> new Operation("Distribuição de Recursos de Emergência", Permissions.LOW_LEVEL);
            case "/emerg" -> new Operation("Ativação de comunicações de Emergência", Permissions.MEDIUM_LEVEL);
            default -> null;
        };

        if (operation == null) {
            System.out.println("ERRO: Operação desconhecida.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Operação desconhecida."));
            return;
        }


        Message message = new Message(username, "null", operation.getName(), true);

        long id = messageService.saveMessage(message);
        if (usersWithPermissionsOnline.isEmpty()) {
            out.println("PENDENTE: Solicitação enviada para aprovação.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação realizada com sucesso: Solicitação enviada para aprovação."));
        } else {
            Group group = groupService.getGroupByName(operation.getRequiredPermission().name());
            out.println("O seu pedido foi enviado para revisão");
            notifyGroup(group, "Pedido pendente: O utilizador " + username + " solicitou a operação '" + operation.getName() + "' com o id " + id + ". Aprove ou rejeite.");
        }
    }

    private void handleApprovalCommand(String action, long id, String username, String requester, PrintWriter out) {
        if (!messageService.isSameMessage(id)) {
            out.println("ERRO: Não há solicitações pendentes para este utilizador.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "ERRO: Não há solicitações pendentes para este utilizador."));

            return;
        }

        try {
            Message deleted = messageService.deleteMessageById(id);
            String operationName = deleted.getContent();

            if (action.equals("/approve")) {
                List<Group> groups = groupService.getAllGroups();
                for (Group group : groups) {
                    notifyGroup(group, operationName);
                }
                NotificationHandler.notify(requester, "SUCESSO: Sua solicitação de operação foi aprovada.");
                out.println("APPROVE: Aprovado com sucesso");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Comando executado: " + operationName + " (por " + username + ")"));

            } else if (action.equals("/reject")) {
                NotificationHandler.notify(requester, "ERRO: Sua solicitação de operação foi rejeitada.");
                out.println("REJECT: Rejectado com sucesso");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação rejeitada."));
            } else {
                out.println("ERRO: Comando desconhecido.");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Comando desconhecido."));
            }
        } catch (EntityNotFoundException e) {
            out.println("ERRO: Comando desconhecido.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Comando desconhecido."));
        }
    }


    private void handleCommandHelper(String username) {
        User user = userService.getUserByName(username);
        System.out.println(user.getName() + user.getPermissions());
        List<String> commandsHighLevel = new ArrayList<>();
        List<String> commandsMediumLevel = new ArrayList<>();
        List<String> commandsLowLevel = new ArrayList<>();
        List<String> commandsNoLevel = new ArrayList<>();


        if (user.getPermissions() == Permissions.HIGH_LEVEL) {
            // Obter a lista de comandos disponíveis para HIGH_LEVEL
            for (HighLevelCommands command : HighLevelCommands.values()) {
                commandsHighLevel.add(command.name() + " - " + command.getDescription());
            }
            for (MediumLevelCommands command : MediumLevelCommands.values()) {
                commandsHighLevel.add(command.name() + " - " + command.getDescription());
            }
            for (LowLevelCommands command : LowLevelCommands.values()) {
                commandsHighLevel.add(command.name() + " - " + command.getDescription());
            }
            for (NoLevelCommands command : NoLevelCommands.values()) {
                commandsHighLevel.add(command.name() + " - " + command.getDescription());
            }
            // Exibir os comandos (exemplo simples)
            for (String temp : commandsHighLevel) {
                NotificationHandler.notify(username, temp);
            }
        } else if (user.getPermissions() == Permissions.MEDIUM_LEVEL) {
            for (MediumLevelCommands command : MediumLevelCommands.values()) {
                commandsMediumLevel.add(command.name() + " - " + command.getDescription());
            }
            for (LowLevelCommands command : LowLevelCommands.values()) {
                commandsMediumLevel.add(command.name() + " - " + command.getDescription());
            }
            for (NoLevelCommands command : NoLevelCommands.values()) {
                commandsMediumLevel.add(command.name() + " - " + command.getDescription());
            }
            for (String temp : commandsMediumLevel) {
                NotificationHandler.notify(username, temp);
            }
        } else if (user.getPermissions() == Permissions.LOW_LEVEL) {
            for (LowLevelCommands command : LowLevelCommands.values()) {
                commandsLowLevel.add(command.name() + " - " + command.getDescription());
            }
            for (NoLevelCommands command : NoLevelCommands.values()) {
                commandsLowLevel.add(command.name() + " - " + command.getDescription());
            }
            for (String temp : commandsLowLevel) {
                NotificationHandler.notify(username, temp);
            }
        } else if (user.getPermissions() == Permissions.NO_LEVEL) {
            for (NoLevelCommands command : NoLevelCommands.values()) {
                commandsNoLevel.add(command.name() + " - " + command.getDescription());
            }
            for (String temp : commandsNoLevel) {
                NotificationHandler.notify(username, temp);
            }
        }
    }
}
