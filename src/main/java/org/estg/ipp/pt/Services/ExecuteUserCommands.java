package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.Message;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static java.lang.System.out;
import static org.estg.ipp.pt.Classes.Interfaces.HelpMessageInterface.*;
import static org.estg.ipp.pt.Notifications.*;
import static org.estg.ipp.pt.Server.*;

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

    public void handleUserCommand(InetAddress serverAddress, String command, String request, String requester, String payload, PrintWriter out,
                                  Map<String, String> pendingApprovals, Set<String> usersWithPermissionsOnline, List<Group> multicastGroups) {

        /*TODO: Adicionar Comando para adicionar pessoas aos grupos personalizados*/
        /*TODO: Adicionar comando para listar grupos que um user pode dar join */
        /*TODO: Adicionar comando para listar todos os comandos disponíveis */
        /*TODO: Adicionar comando para mandar menssagem para um utilizador especifico (/chat)*/
        switch (command) {
            case "/evac", "/resdist", "/emerg" ->
                    processOperationCommand(payload, command, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
            case "/approve" -> {
                Matcher approveMatcher = RegexPatternsCommands.APPROVE.matcher(request);
                if (approveMatcher.matches()) {
                    String help = approveMatcher.group("help");
                    String username = approveMatcher.group("username");

                    if (help != null) {
                        out.println(APPROVE_HELP);
                    } else if (requester != null) {
                        handleApprovalCommand(command, username, requester, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
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
                    String username = rejectMatcher.group("username");

                    if (help != null) {
                        out.println(REJECT_HELP);
                    } else if (requester != null) {
                        handleApprovalCommand(command, username, requester, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
                    } else {
                        out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                    }
                } else {
                    out.println("ERRO: Formato inválido para REJECT. Use -h para descobrir os parâmetros");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para REJECT"));
                }
            }
            case "/export" -> {
                processExport(request, out);
            }
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
                    String username = chatMatcher.group("username");
                    if (targetUsername != null && !targetUsername.isEmpty()) {
                        Socket sender = Server.getUserSocket(username);

                        User targetUser = userService.getUserByName(targetUsername);

                        Socket receiver = Server.getUserSocket(targetUsername);

                        ServerSocket privateServerSocket = null;

                        try {
                            privateServerSocket = new ServerSocket(0);  // Create a ServerSocket to listen on an available port
                            int privatePort = privateServerSocket.getLocalPort(); // Get the dynamically assigned port
                            System.out.println("Private chat will use port: " + privatePort);

                            // Notify both parties about the same connection details
                            String connectionInfo = sender.getInetAddress().getHostAddress() + ":" + privatePort;
                            out.println("CHAT_START:" + connectionInfo);  // Notify the sender
                            notifyUser(targetUsername, "CHAT_REQUEST:" + connectionInfo, usersWithPermissionsOnline, targetUser.getCurrentGroup(), pendingApprovals);

                            // Wait for the receiver to connect to this privateServerSocket
                            sender = privateServerSocket.accept();  // Wait for the receiver to connect
                            System.out.println("Sender connected on port: " + privatePort);

                            receiver = privateServerSocket.accept();
                            System.out.println("Receiver connected on port: " + privatePort);

                            // Start a thread to handle the private chat
                            Socket finalReceiverSocket = receiver;
                            Socket finalSenderSocket = sender;
                            ServerSocket finalPrivateServerSocket = privateServerSocket;
                            new Thread(() -> handlePrivateChat(finalSenderSocket, finalReceiverSocket, finalPrivateServerSocket)).start();
                        } catch (IOException e) {
                            System.err.println("Error creating server socket for private chat: " + e.getMessage());
                            out.println("ERRO: Could not create private chat server.");
                        }
                    } else {
                        out.println("ERRO: Por favor, forneça o nome de utilizador do destinatário. Use -h para ajuda.");
                        logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /chat"));

                    }
                } else {
                    out.println("ERRO: Formato inválido para /chat. Use -h para ajuda.");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato inválido para /chat"));
                }
            }
        }
    }

    private void handlePrivateChat(Socket senderSocket, Socket receiverSocket, ServerSocket privateServerSocket) {
        try { // Wait for the receiver to connect
            BufferedReader senderIn = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
            PrintWriter senderOut = new PrintWriter(senderSocket.getOutputStream(), true);
            BufferedReader receiverIn = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
            PrintWriter receiverOut = new PrintWriter(receiverSocket.getOutputStream(), true);

            System.out.println("Private chat connection established between sender and receiver.");

            // Threads for bidirectional communication
            Thread senderToReceiver = new Thread(() -> {
                try {
                    String message;
                    while ((message = senderIn.readLine()) != null) {
                        System.out.println("Sender: " + message);
                        receiverOut.println(message);  // Forward to receiver
                    }
                } catch (IOException e) {
                    System.err.println("Error forwarding message from sender to receiver: " + e.getMessage());
                }
            });

            Thread receiverToSender = new Thread(() -> {
                try {
                    String message;
                    while ((message = receiverIn.readLine()) != null) {
                        System.out.println("Receiver: " + message);
                        senderOut.println(message);  // Forward to sender
                    }
                } catch (IOException e) {
                    System.err.println("Error forwarding message from receiver to sender: " + e.getMessage());
                }
            });

            // Start the threads
            senderToReceiver.start();
            receiverToSender.start();

            // Wait for threads to finish
            senderToReceiver.join();
            receiverToSender.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error handling private chat: " + e.getMessage());
        } finally {
            try {
                privateServerSocket.close();  // Close the server socket after chat ends
                senderSocket.close();         // Close sender's socket
            } catch (IOException e) {
                System.err.println("Error closing sockets: " + e.getMessage());
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

        /*TODO: Verificar permissões*/
        //if (!groupService.isUserInGroup(name, user.getId())) {
        // groupService.addUserToGroup(name, user);

        // Buscar o grupo com os parâmetros fornecidos
        Group group = groupService.getUserGroupByNameAndVerify(user.getId(), name); // Método para buscar o grupo
        if (group == null) {
            out.println("ERRO: Grupo não encontrado");
            return;
        }

        out.println("SUCESSO: Usuário " + username + " entrou no grupo " + name);

        try {
            userService.joinGroup(user, group);
        } catch (IllegalArgumentException e) {
            out.println("ERRO: " + e.getMessage());
            return;
        }
        // Agora, permitir que o usuário entre no chat
        try {
            Chat.startChat(group.getAddress(), group.getPort(), username); // Chama o método para iniciar o chat multicast
        } catch (IOException e) {
            out.println("ERRO: Falha ao tentar entrar no chat: " + e.getMessage());
        }
    }

    private void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out) {
        User userWithPermissions = userService.getUserByName(username); // Método para encontrar o usuário pelo nome de usuário
        if (userWithPermissions == null) {
            out.println("ERRO: Usuário não encontrado");
            return;
        }
        System.out.println(userWithPermissions.getPermissions());
        if (Permissions.fromPermissions(userWithPermissions.getPermissions()) >= Permissions.fromPermissions(Permissions.HIGH_LEVEL)) {
            userService.updateUserPermissions(name, permission);
            out.println("SUCESSO: Usuário " + name + " promovido para " + permission.name());
        } else {
            out.println("ERRO: Usuário sem permissões para usar este comando");
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

    private void processOperationCommand(String username, String operationName, PrintWriter out,
                                         Map<String, String> pendingApprovals,
                                         Set<String> usersWithPermissionsOnline, List<Group> multicastGroups) {
        User user = userService.getUserByName(username);
        if (user == null) {
            out.println("ERRO: Utilizador não encontrado.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Utilizador não encontrado."));
            return;
        }

        Operation operation = switch (operationName) {
            case "/evac" -> new Operation("Operação de evacuação em massa", Permissions.HIGH_LEVEL);
            case "/resdist" -> new Operation("Distribuição de Recursos de Emergência", Permissions.LOW_LEVEL);
            case "/emerg" -> new Operation("Ativação de comunicações de Emergência", Permissions.MEDIUM_LEVEL);
            default -> null;
        };

        if (operation == null) {
            out.println("ERRO: Operação desconhecida.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Operação desconhecida."));
            return;
        }

        if (user.getPermissions().ordinal() >= operation.getRequiredPermission().ordinal()) {
            // Permissão suficiente - executar a operação
            sendNotificationToGroups("Comando executado: " + operation.getName() + " (por " + username + ")", multicastGroups);
            out.println("SUCESSO: Operação realizada.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação realizada com sucesso: Comando executado: \" + operation.getName() + \" (por \" + username + \")\""));
        } else {
            pendingApprovals.put(username, operation.getName());
            if (usersWithPermissionsOnline.isEmpty()) {
                out.println("PENDENTE: Solicitação enviada para aprovação.");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação realizada com sucesso: Solicitação enviada para aprovação."));

                notifyUser(username, "PENDENTE: Solicitação enviada para aprovação.", usersWithPermissionsOnline, user.getCurrentGroup(), pendingApprovals);
            } else {
                for (String approver : usersWithPermissionsOnline) {
                    User approverUser = userService.getUserByName(approver);
                    notifyUser(approver, "Solicitação para aprovação do comando '" + operation + "' por " + username, usersWithPermissionsOnline, approverUser.getCurrentGroup(), pendingApprovals);
                }
                out.println("PENDENTE: Solicitação enviada para aprovação.");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação realizada com sucesso: Solicitação enviada para aprovação."));
            }
        }
    }

    private void handleApprovalCommand(String action, String username, String requester, PrintWriter out,
                                       Map<String, String> pendingApprovals,
                                       Set<String> usersWithPermissionsOnline,
                                       List<Group> multicastGroups) {
        User user = userService.getUserByName(requester);
        if (!pendingApprovals.containsKey(requester)) {
            notifyUser(requester, "ERRO: Não há solicitações pendentes para este utilizador.", usersWithPermissionsOnline, user.getCurrentGroup(), pendingApprovals);
            out.println("ERRO: Comando desconhecido.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Comando desconhecido."));

            return;
        }

        String operationName = pendingApprovals.remove(requester);


        if (action.equals("/approve")) {
            sendNotificationToGroups("Comando executado: " + operationName + " (por " + username + ")", multicastGroups);
            notifyUser(requester, "SUCESSO: Sua solicitação de operação foi aprovada.", usersWithPermissionsOnline, user.getCurrentGroup(), pendingApprovals);
            out.println("APPROVE: Aprovado com sucesso");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Comando executado: " + operationName + " (por " + username + ")"));

        } else if (action.equals("/reject")) {
            notifyUser(requester, "ERRO: Sua solicitação de operação foi rejeitada.", usersWithPermissionsOnline, user.getCurrentGroup(), pendingApprovals);
            notifyUser(username, "SUCESSO: Operação rejeitada.", usersWithPermissionsOnline, user.getCurrentGroup(), pendingApprovals);
            out.println("Reject: Rejectado com sucesso");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação rejeitada."));
        } else {
            notifyUser(username, "ERRO: Comando desconhecido. Use APPROVE ou REJECT.", usersWithPermissionsOnline, user.getCurrentGroup(), pendingApprovals);
            out.println("ERRO: Comando desconhecido.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Comando desconhecido."));
        }
    }

    public static void saveNotificationForLater(String username, String message, Map<String, String> pendingApprovals) {
        // Aqui você poderia salvar as notificações que não puderam ser enviadas
        // Exemplo: armazenar em uma tabela no banco de dados ou em uma lista temporária
        out.println("Notificação salva para " + username + ": " + message);

        // Exemplo de armazenamento simples em um Map ou Lista
        // Você pode usar uma abordagem diferente dependendo de como deseja salvar as notificações
        pendingApprovals.put(username, message);  // Mapa fictício para armazenar as notificações pendentes
        // Se você estiver usando um banco de dados, faria a inserção aqui
    }
}
