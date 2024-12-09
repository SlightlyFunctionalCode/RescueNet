package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.*;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessCommandsInterface;
import org.estg.ipp.pt.ServerSide.Services.NotificationHandler;
import org.estg.ipp.pt.ServerSide.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static org.estg.ipp.pt.ServerSide.Classes.HelpMessages.*;

@Component
public class ExecuteUserCommands {
    @Autowired
    private LogService logService;
    @Autowired
    private MessageService messageService;

    @Autowired
    private ProcessCommandsInterface processCommands;

    public void handleUserCommand(String command, String request, String requester, String payload, PrintWriter out,
                                  ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) throws IOException {

        /*TODO: Adicionar comando para alertar utilizadores */
        switch (command) {
            case "/evac", "/resdist", "/emerg" ->
                    processCommands.processOperationCommand(payload, command, out, usersWithPermissionsOnline);
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
                        processCommands.handleApprovalCommand(command, id, username, requester, out);
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
                        processCommands.handleApprovalCommand(command, id, username, requester, out);
                    } else {
                        out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                }
            }
            case "/export" -> processCommands.processExport(request, out);
            case "/join" -> {
                Matcher joinMatcher = RegexPatternsCommands.JOIN.matcher(request);
                if (joinMatcher.matches()) {
                    String help = joinMatcher.group("help");
                    String name = joinMatcher.group("name");

                    if (help != null) {
                        out.println(JOIN_HELP);
                    } else if (name != null) {
                        processCommands.processJoinCommand(payload, name, out);
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
                            processCommands.processChangePermissionCommand(payload, name, permissions, out);
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
                        processCommands.processCreateGroupCommand(payload, name, publicOrPrivate, out);
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
                    String help = chatMatcher.group("help");
                    String targetUsername = chatMatcher.group("targetUsername");
                    String message = chatMatcher.group("message");
                    String username = chatMatcher.group("username");


                    if (help != null) {
                        out.println(CHAT_HELP);
                    } else if (targetUsername != null && !targetUsername.isEmpty() && message != null && !message.isEmpty()) {
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
                    String name = commandsHelper.group("name");
                    processCommands.handleCommandHelper(name, out);
                } else {
                    out.println("ERRO: Formato inválido para /commands. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /create_group"));
                }
            }
            case "/addToGroup" -> {
                System.out.println(request);
                Matcher addToGroup = RegexPatternsCommands.ADD_TO_GROUP.matcher(request);
                if (addToGroup.matches()) {
                    out.println("ADDTOGROUP START");
                    String user_request = addToGroup.group("username");
                    String userToAdd = addToGroup.group("userToAdd");
                    String group = addToGroup.group("group");
                    processCommands.handleAddToGroup(user_request, userToAdd, group, out);
                } else {
                    out.println("ERRO: Formato inválido para /addToGroup");
                }
            }
            case "/groups" -> {
                Matcher listGroups = RegexPatternsCommands.LIST_GROUPS.matcher(request);
                if (listGroups.matches()) {
                    String username = listGroups.group("username");
                    processCommands.handleListGroups(username, out);
                } else {
                    out.println("ERRO: Formato inválido para /addToGroup");
                }
            }
            case "/leave" -> {
                Matcher listGroups = RegexPatternsCommands.LEAVE_GROUP.matcher(request);
                if (listGroups.matches()) {
                    String help = listGroups.group("help");
                    String groupName = listGroups.group("groupName");
                    String username = listGroups.group("username");

                    if (help != null) {
                        out.println(LEAVE_HELP);
                    } else if (groupName != null && !groupName.isEmpty() && username != null && !username.isEmpty()) {
                        processCommands.handleLeaveGroup(username, groupName, out);
                    }
                } else {
                    out.println("ERRO: Formato inválido para /addToGroup");
                }
            }
            default -> {
                out.println("ERRO: Comando de utilizador inválido");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Comando de utilizador inválido"));
            }
        }
    }
}
