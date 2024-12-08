package org.estg.ipp.pt.ServerSide.Classes;

import jakarta.persistence.EntityNotFoundException;
import org.estg.ipp.pt.Classes.Enum.*;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessCommandsInterface;
import org.estg.ipp.pt.ServerSide.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static org.estg.ipp.pt.ServerSide.Classes.HelpMessages.EXPORT_HELP;
import static org.estg.ipp.pt.ServerSide.Services.NotificationHandler.notifyGroup;

@Component
public class ProcessCommands implements ProcessCommandsInterface {
    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private LogService logService;

    public void processExport(String request, PrintWriter out) {
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

    public void processExportByDateRangeCommand(LocalDateTime startDate, LocalDateTime endDate, String username, PrintWriter out) {
        // Generate the endpoint URL
        String url = "http://localhost:8080/download-pdf-report?startDate=" + startDate + "&endDate=" + endDate;

        processExportURL(url, username, out);
    }

    public void processExportByDateRangeAndTagCommand(LocalDateTime startDate, LocalDateTime endDate, TagType tagType, String username, PrintWriter out) {
        // Generate the endpoint URL
        String url = "http://localhost:8080/download-pdf-report?startDate=" + startDate + "&endDate=" + endDate + "&tag=" + tagType.name();

        processExportURL(url, username, out);
    }

    public void processExportByTagCommand(TagType tagType, String username, PrintWriter out) {
        // Generate the endpoint URL
        String url = "http://localhost:8080/download-pdf-report?tag=" + tagType.name();

        processExportURL(url, username, out);
    }

    public void processExportURL(String url, String username, PrintWriter out) {
        // Log the generated URL
        System.out.println("Generated URL for download: " + url);

        // Notify the client to download the file
        out.println("SUCESSO: O pdf foi gerado com sucesso. Por favor, faça o download aqui: " + url);

        // Save log for success
        logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "O pdf gerado por " + username + " foi gerado com sucesso"));
    }

    public void processJoinCommand(String username, String name, PrintWriter out) {
        User user = userService.getUserByName(username);
        if (user == null) {
            out.println("ERRO: Usuário não encontrado");
            return;
        }
        Group group = groupService.getGroupByName(name);
        if (group == null) {
            out.println("ERRO: Grupo não encontrado");
            return;
        }
        if (group.isPublic() && Permissions.fromPermissions(group.getRequiredPermissions()) <= Permissions.fromPermissions(user.getPermissions())) {
            System.out.println("SUCESSO: Usuário " + username + " entrou no grupo " + name);
            try {
                userService.joinGroup(user, group);
            } catch (IllegalArgumentException e) {
                out.println("ERRO: " + e.getMessage());
                return;
            }
            String connectionInfo = group.getAddress() + ":" + group.getPort();
            out.println("CHAT_GROUP:" + connectionInfo);
        } else {
            out.println("Grupo é privado ou não tem permissões para dar join nesse grupo");
        }
    }

    public void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out) {
        User userWithPermissions = userService.getUserByName(username);
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

    public void processCreateGroupCommand(String username, String name, String publicOrPrivate, PrintWriter out) {
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

    public void processOperationCommand(String username, String command, PrintWriter out,
                                        ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) {
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

    public void handleApprovalCommand(String action, long id, String username, String requester, PrintWriter out) {
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


    public void handleCommandHelper(String username) {
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
