package org.estg.ipp.pt.ServerSide.Classes;

import jakarta.persistence.EntityNotFoundException;
import org.estg.ipp.pt.Classes.Enum.*;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Server;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessUserCommands;
import org.estg.ipp.pt.ServerSide.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static org.estg.ipp.pt.ServerSide.Classes.HelpMessages.*;
import static org.estg.ipp.pt.ServerSide.Services.NotificationHandler.notifyGroup;

@Component
public class ProcessUserCommandsImpl implements ProcessUserCommands {
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

        if (exportMatcher.matches()) {
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
        String url = "http://localhost:8080/download-pdf-report?tag=" + tagType.name();

        processExportURL(url, username, out);
    }

    public void processExportURL(String url, String username, PrintWriter out) {
        System.out.println("Generated URL for download: " + url);

        out.println("SUCESSO: O pdf foi gerado com sucesso. Por favor, faça o download aqui: " + url);

        logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "O pdf gerado por " + username + " foi gerado com sucesso"));
    }

    @Transactional
    public void processJoinCommand(String username, String name, PrintWriter out) {
        User user = userService.getUserByName(username);
        if (user == null) {
            out.println("ERRO: Utilizador não encontrado");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao entrar no grupo"));
            return;
        }

        Group group = groupService.getGroupByName(name);

        if (group == null) {
            out.println("ERRO: Grupo não encontrado");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou entrar num grupo que não existe"));
            return;
        }

        if (group.isPublic() && Permissions.fromPermissions(group.getRequiredPermissions()) <= Permissions.fromPermissions(user.getPermissions())
                || !group.isPublic() && group.getUsers().contains(user)) {
            System.out.println("SUCESSO: Utilizador " + username + " entrou no grupo " + name);

            try {
                userService.joinGroup(user, group);
            } catch (IllegalArgumentException e) {
                out.println("ERRO: " + e.getMessage());
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao entrar no grupo: " + e.getMessage()));

                return;
            }
            String connectionInfo = group.getAddress() + ":" + group.getPort();
            out.println("CHAT_GROUP:" + connectionInfo);
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " entrou no grupo " + name));
        } else {
            out.println("Grupo é privado ou não tem permissões para dar join nesse grupo");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou entrar num grupo privado, ou num que não tem permissões para entrar"));
        }
    }

    public void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out) {
        User userWithPermissions = userService.getUserByName(username);
        if (userWithPermissions == null) {
            out.println("ERRO: Utilizador não encontrado");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao mudar as permissões de " + name));
            return;
        }
        User userUpdated = userService.getUserByName(name);
        List<Group> groups = groupService.getAllGroups();
        System.out.println(userWithPermissions.getPermissions());
        userService.updateUserPermissions(name, permission);

        if (Permissions.fromPermissions(userWithPermissions.getPermissions()) >= Permissions.fromPermissions(Permissions.HIGH_LEVEL)) {
            groupService.removeUserFromGroup(userUpdated, permission);
        } else {
            for (Group group : groups) {
                if (group.isPublic() && Permissions.fromPermissions(userUpdated.getPermissions()) > Permissions.fromPermissions(group.getRequiredPermissions())) {
                    groupService.addUserToGroup(group.getName(), userUpdated);
                }
            }
        }
        out.println("SUCESSO: Utilizador " + name + " promovido para " + permission.name());
        logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " promovido para " + permission.name()));
    }

    public void processCreateGroupCommand(String username, String name, String publicOrPrivate, PrintWriter out) {
        User userWithPermissions = userService.getUserByName(username);
        if (userWithPermissions == null) {
            out.println("ERRO: Utilizador não encontrado");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao criar um grupo, porque nao foi possível encontrar o utilizador"));

            return;
        }

        if (!publicOrPrivate.equalsIgnoreCase("public") && !publicOrPrivate.equalsIgnoreCase("private")) {
            out.println("Verifique se o tipo de grupo está public ou private");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao criar um grupo, pois não definiu corretamente a privacidade do grupo"));

            return;
        }

        try {
            Group newGroup = groupService.addCustomGroup(userWithPermissions.getId(), name, publicOrPrivate);
            if (newGroup == null) {
                out.println("ERRO: Grupo não pode ser criado");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao criar um grupo"));
            } else {
                groupService.addUserToGroup(newGroup.getName(), userWithPermissions);
                out.println("SUCESSO: Grupo " + name + " criado com sucesso");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " criou um grupo"));
            }
        } catch (Exception e) {
            out.println("ERRO: " + e.getMessage());
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao criar um grupo"));
        }
    }

    public void processOperationCommand(String username, String command, PrintWriter out,
                                        ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) {
        User user = userService.getUserByName(username);
        if (user == null) {
            System.out.println("ERRO: Utilizador não encontrado.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Utilizador não encontrado ao executar comando por " + username));
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
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou executar uma operação inválida"));
            return;
        }

        if (command.equals("/evac")) {
            if (Permissions.fromPermissions(user.getPermissions()) < Permissions.fromPermissions(Permissions.MEDIUM_LEVEL)) {
                out.println("Não tem permissões para usar este comando!");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou executar o comando sem as permissões necessárias"));

                return;
            }
        } else if (command.equals("/emerg")) {
            if (Permissions.fromPermissions(user.getPermissions()) < Permissions.fromPermissions(Permissions.LOW_LEVEL)) {
                out.println("Não tem permissões para usar este comando!");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou executar o comando sem as permissões necessárias"));

                return;
            }
        }

        Message message = new Message(username, "null", operation.getName(), true);

        long id = messageService.saveMessage(message);
        if (usersWithPermissionsOnline.isEmpty()) {
            out.println("PENDENTE: Solicitação enviada para aprovação.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " executou o comando " + operation.getName()));
        } else {
            Group group = groupService.getGroupByName(operation.getRequiredPermission().name());
            out.println("O seu pedido foi enviado para revisão");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " executou o comando " + operation.getName()));
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

        if (messageService.getContent(id).equals("Operação de evacuação em massa")) {
            permissions = Permissions.HIGH_LEVEL;
        } else if (messageService.getContent(id).equals("Ativação de comunicações de Emergência")) {
            permissions = Permissions.MEDIUM_LEVEL;
        } else if (messageService.getContent(id).equals("Distribuição de Recursos de Emergência")) {
            permissions = Permissions.LOW_LEVEL;
        }

        if (Permissions.fromPermissions(user.getPermissions()) < Permissions.fromPermissions(permissions)) {
            out.println("Não tens as permissões necessárias para aprovar ou rejeitas este pedido!");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou aprovar o comando sem as permissões necessárias"));
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
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Ocorreu um erro ao mostrar os comandos, por ser uma permissão desconhecida"));
        }
    }

    public void handleAddToGroup(String username, String requester, String group, PrintWriter out) {
        User user = userService.getUserByName(username);
        User userToAdd = userService.getUserByName(requester);

        if (user == null || userToAdd == null) {
            out.println("O utilizador que pretende adicionar ao grupo não existe");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou adicionar um utilizador que não existe ao grupo"));
            return;
        }

        try {
            Group groupToAdd = groupService.getGroupByName(group);

            if (!Objects.equals(user.getId(), groupToAdd.getCreatedBy())) {
                out.println("Este groupo não foi criado por si!");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou adicionar um utilizador a um grupo que não foi criado por si"));
                return;
            }

            groupService.addUserToGroup(groupToAdd.getName(), userToAdd);
            out.println("SUCESSO: O utilizador foi adicionado ao grupo");
            NotificationHandler.notify(userToAdd.getName(), "Você foi adicionado ao grupo: " + group);
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " adicionou " + requester + " ao grupo " + group));

        } catch (IllegalArgumentException e) {
            out.println("ERRO: " + e.getMessage());
        }
    }

    public void handleListGroups(String username, PrintWriter out) {
        User user = userService.getUserByName(username);

        if (user == null) {
            out.println("Erro: Erro ao mostrar grupos");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Erro ao mostrar grupos a" + username));

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
        logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Sucesso ao mostrar grupos a" + username));

    }

    public void handleAlertMessage(String username, String message, PrintWriter out) {
        User user = userService.getUserByName(username);

        if (Permissions.fromPermissions(user.getPermissions()) != Permissions.fromPermissions(Permissions.HIGH_LEVEL)) {
            out.println("Não tens permissão para usar este comando!");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou usar alert sem permissões"));

            return;
        }

        List<Group> groups = groupService.getAllGroups();
        for (Group group : groups) {
            notifyGroup(group, "**ALERTA: " + message + "**");
        }
        out.println("EXECUTADO COM SUCESSO");
        logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " executou o comando alerta"));
    }

    public void handleLeaveGroup(String username, String groupName, PrintWriter out) {
        User user = userService.getUserByName(username);
        Group group;
        try {
            if (user == null) {
                out.println("Erro: Erro ao sair do Grupo");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao sair do Grupo"));

                return;
            } else if (groupName.equals("GERAL") || groupName.equals("HIGH_LEVEL") || groupName.equals("MEDIUM_LEVEL") || groupName.equals("LOW_LEVEL")) {
                out.println("Erro: Não é possível sair dos grupos base");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " tentou sair de um grupo base"));

                return;
            }

            group = groupService.getUserGroupByNameAndVerify(user.getId(), groupName);

            groupService.leaveGroup(user, group);

            out.println("Você saiu do grupo");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " saiu do grupo " + group.getName()));

        } catch (Exception e) {
            out.println("Erro: Erro ao sair do Grupo");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao sair do Grupo"));

        }
    }

    public void handleLogout(String username, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline, PrintWriter out) {
        try {
            Server.removeUserSocket(username);

            out.println("Você saiu do programa");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, username + " fez logout"));

            User user = userService.getUserByName(username);

            if (Permissions.fromPermissions(user.getPermissions()) >= Permissions.fromPermissions(Permissions.LOW_LEVEL)) {
                usersWithPermissionsOnline.remove(username);
            }

        } catch (Exception e) {
            out.println("Erro: Erro ao sair do Grupo");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, username + " teve um erro ao fazer logout"));
        }
    }

}