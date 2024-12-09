package org.estg.ipp.pt.ServerSide.Classes;

import jakarta.persistence.EntityNotFoundException;
import org.estg.ipp.pt.Classes.Enum.*;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessCommandsInterface;
import org.estg.ipp.pt.ServerSide.Repositories.MessageRepository;
import org.estg.ipp.pt.ServerSide.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static org.estg.ipp.pt.ServerSide.Classes.HelpMessages.*;
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
    @Autowired
    private MessageRepository messageRepository;

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

    @Transactional
    public void processJoinCommand(String username, String name, PrintWriter out) {
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
        if (group.isPublic() && Permissions.fromPermissions(group.getRequiredPermissions()) <= Permissions.fromPermissions(user.getPermissions())
                || !group.isPublic() && group.getUsers().contains(user)) {
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

    public void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out) {
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
            out.println("Erro: " + command);
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Operação desconhecida."));
            return;
        }

        if (command.equals("/evac")) {
            if (Permissions.fromPermissions(user.getPermissions()) < Permissions.fromPermissions(Permissions.MEDIUM_LEVEL)) {
                out.println("Não tem permissões para usar este comando!");
                return;
            }
        } else if (command.equals("/emerg")) {
            if (Permissions.fromPermissions(user.getPermissions()) < Permissions.fromPermissions(Permissions.LOW_LEVEL)) {
                out.println("Não tem permissões para usar este comando!");
                return;
            }
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

        User user = userService.getUserByName(username);
        Permissions permissions = Permissions.NO_LEVEL;

        if(messageService.getContent(id).equals("Operação de evacuação em massa")){
            permissions = Permissions.HIGH_LEVEL;
        }else if(messageService.getContent(id).equals("Ativação de comunicações de Emergência")){
            permissions = Permissions.MEDIUM_LEVEL;
        }else if(messageService.getContent(id).equals("Distribuição de Recursos de Emergência")){
            permissions = Permissions.LOW_LEVEL;
        }

        if(Permissions.fromPermissions(user.getPermissions()) < Permissions.fromPermissions(permissions)) {
            out.println("Não tens as permissões necessárias para aprovar ou rejeitas este pedido!");
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


    public void handleCommandHelper(String username, PrintWriter out) {
        User user = userService.getUserByName(username);

        switch (user.getPermissions()) {
            case HIGH_LEVEL:
                out.println(COMMANDS_HIGH);
                break;
            case MEDIUM_LEVEL:
                out.println(COMMANDS_MEDIUM);
                break;
            case LOW_LEVEL:
                out.println(COMMANDS_LOW);
                break;
            case NO_LEVEL:
                out.println(COMMANDS_DEFAULT);
                break;
            default:
                out.println("Permissão desconhecida.");
        }

    }

    public void handleAddToGroup(String username, String requester, String group, PrintWriter out) {
        User user = userService.getUserByName(username);
        User userToAdd = userService.getUserByName(requester);
        Group groupToAdd = groupService.getGroupByName(group);

        if (!Objects.equals(user.getId(), groupToAdd.getId())) {
            out.println("Este group não foi criado por si!");
        }

        groupService.addUserToGroup(groupToAdd.getName(), userToAdd);

    }

    public void handleListGroups(String username, PrintWriter out) {
        User user = userService.getUserByName(username);

        if (user == null) {
            out.println("Erro: Erro ao mostrar grupos");
            return;
        }

        List<Group> allGroups = groupService.getAllGroups();

        StringBuilder result = new StringBuilder("--HELP--\nLista de Grupos Disponíveis:\n");

        int counter = 0;
        for (Group group : allGroups) {
            if (Permissions.fromPermissions(group.getRequiredPermissions()) <= Permissions.fromPermissions(user.getPermissions())) {
                if (group.getName().equals("HIGH_LEVEL") || group.getName().equals("MEDIUM_LEVEL") ||
                        group.getName().equals("LOW_LEVEL") || group.getName().equals("GERAL")
                        || groupService.isUserInGroup(group.getName(), user.getId())
                        || group.isPublic()) {
                    result.append("- ").append(group.getName()).append("\n");
                    counter++;
                }
            }
        }

        if (counter == 0) {
            result.append("Não existem grupos disponíveis\n");
        }
        result.append("--END HELP--");
        out.println(result);
    }

    public void handleAlertMessage(String username, String message, PrintWriter out) {
        User user = userService.getUserByName(username);

        if(Permissions.fromPermissions(user.getPermissions()) != Permissions.fromPermissions(Permissions.HIGH_LEVEL)) {
            out.println("Não tens permissão para usar este comando!");
            return;
        }

        List<Group> groups = groupService.getAllGroups();
        for (Group group : groups) {
            notifyGroup(group, "ALERTA: " + message);
        }
        out.println("EXECUTADO COM SUCESSO");
    }

    public void handleLeaveGroup(String username, String groupName, PrintWriter out) {
        User user = userService.getUserByName(username);
        Group group;
        try {
            if (user == null) {
                out.println("Erro: Erro ao sair do Grupo");
                return;
            } else if (groupName.equals("GERAL") || groupName.equals("HIGH_LEVEL") || groupName.equals("MEDIUM_LEVEL") || groupName.equals("LOW_LEVEL")) {
                out.println("Erro: Não é possível sair dos grupos base");
                return;
            }

            group = groupService.getUserGroupByNameAndVerify(user.getId(), groupName);

            groupService.leaveGroup(user, group);

            out.println("Você saiu do grupo");
        } catch (Exception e) {
            out.println("Erro: Erro ao sair do Grupo");
        }
    }
}
