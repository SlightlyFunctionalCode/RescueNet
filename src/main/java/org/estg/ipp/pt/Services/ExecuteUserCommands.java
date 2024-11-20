package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static java.lang.System.out;
import static org.estg.ipp.pt.Notifications.*;
import static org.estg.ipp.pt.Notifications.notifyUser;
import static org.estg.ipp.pt.Server.pendingApprovals;
import static org.estg.ipp.pt.Server.usersWithPermissionsOnline;

@Component
public class ExecuteUserCommands {
    @Autowired
    private UserService userService;
    @Autowired
    private LogService logService;
    @Autowired
    public GroupService groupService;

    public void handleUserCommand(String command, String request, String requester, String payload, PrintWriter out,
                                  Map<String, String> pendingApprovals,
                                  Set<String> usersWithPermissionsOnline, List<Group> multicastGroups) {
        switch (command) {
            case "/evac", "/resdist", "/emerg" ->
                    processOperationCommand(payload, command, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
            case "/approve" -> {
                Matcher approveMatcher = RegexPatternsCommands.APPROVE.matcher(request);
                if (approveMatcher.matches()) {
                    String username = approveMatcher.group("username");
                    handleApprovalCommand(command, username, requester, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
                } else {
                    out.println("ERRO: Formato inválido para APPROVE");
                }
            }
            case "/reject" -> {
                Matcher rejectMatcher = RegexPatternsCommands.REJECT.matcher(request);
                if (rejectMatcher.matches()) {
                    String username = rejectMatcher.group("username");
                    handleApprovalCommand(command, username, requester, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
                } else {
                    out.println("ERRO: Formato inválido para REJECT");
                }
            }
            case "/export" -> {
                processExport(request, out);
            }
            case "/join" -> {
                Matcher joinMatcher = RegexPatternsCommands.JOIN.matcher(request);
                if (joinMatcher.matches()) {
                    String name = joinMatcher.group("name");
                    System.out.println(name);
                    System.out.println(payload);
                    processJoinCommand(payload, name, out);
                }
            }
            default -> out.println("ERRO: Comando de utilizador inválido");
        }
    }

    public void processExport(String request, PrintWriter out) {
        Matcher exportDateTagMatcher = RegexPatternsCommands.EXPORT_DATE_TAG.matcher(request);
        Matcher exportTagMatcher = RegexPatternsCommands.EXPORT_TAG.matcher(request);
        Matcher exportDateMatcher = RegexPatternsCommands.EXPORT_DATE.matcher(request);
        Matcher exportHelpMatcher = RegexPatternsCommands.EXPORT_HELP.matcher(request);

        if (exportHelpMatcher.matches()) {
            String help = """
                    INFO: Os parâmetros que podem ser executados por /export são:
                    
                    <startDate> data e hora inicial (formato DD-MM-yyyyThh:mm:ss)
                    
                    <endDate> data e hora final (formato DD-MM-yyyyThh:mm:ss)
                    
                    <tag> tag do tipo TagType\r""";
            out.println(help);
        } else if (exportDateTagMatcher.matches()) {
            try {
                LocalDateTime startDate = LocalDateTime.parse(exportDateTagMatcher.group("startDate"));
                LocalDateTime endDate = LocalDateTime.parse(exportDateTagMatcher.group("endDate"));
                TagType tag = TagType.valueOf(exportDateTagMatcher.group("tag"));
                String username = exportDateTagMatcher.group("username");

                processExportByDateRangeAndTagCommand(startDate, endDate, tag, username, out);
            } catch (DateTimeParseException ex) {
                out.println("ERRO: Data inválida para Export. Deve ser YYYY-MM-DDThh:mm:ss");
            } catch (IllegalArgumentException ie) {
                out.println("ERRO: Tag inválida");
            }
        } else if (exportDateMatcher.matches()) {
            try {
                LocalDateTime startDate = LocalDateTime.parse(exportDateMatcher.group("startDate"));
                LocalDateTime endDate = LocalDateTime.parse(exportDateMatcher.group("endDate"));
                String username = exportDateMatcher.group("username");

                processExportByDateRangeCommand(startDate, endDate, username, out);
            } catch (DateTimeParseException ex) {
                out.println("ERRO: Data inválida para Export. Deve ser YYYY-MM-DDThh:mm:ss");
            }
        } else if (exportTagMatcher.matches()) {
            try {
                TagType tag = TagType.valueOf(exportTagMatcher.group("tag"));
                String username = exportTagMatcher.group("username");

                processExportByTagCommand(tag, username, out);
            } catch (IllegalArgumentException ie) {
                out.println("ERRO: Tag inválida");
            }
        } else {
            out.println("ERRO: Formato inválido para Export");
        }
    }

    private void processExportByDateRangeCommand(LocalDateTime startDate, LocalDateTime endDate, String username, PrintWriter out) {
        try {
            // Generate the endpoint URL
            String url = "http://localhost:8080/download-pdf-report?startDate=" + startDate + "&endDate=" + endDate;

            // Log the generated URL
            System.out.println("Generated URL for download: " + url);

            // Notify the client to download the file
            out.println("SUCESSO: O pdf foi gerado com sucesso. Por favor, faça o download aqui: " + url);

            // Save log for success
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "O pdf gerado por " + username + " foi gerado com sucesso"));
        } catch (Exception e) {
            // Handle exceptions
            out.println("ERRO: Falha ao gerar o relatório PDF.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Falha ao gerar o relatório PDF para " + username));
        }
    }

    private void processExportByDateRangeAndTagCommand(LocalDateTime startDate, LocalDateTime endDate, TagType tagType, String username, PrintWriter out) {
        try {
            // Generate the endpoint URL
            String url = "http://localhost:8080/download-pdf-report?startDate=" + startDate + "&endDate=" + endDate + "&tag=" + tagType.name();

            // Log the generated URL
            System.out.println("Generated URL for download: " + url);

            // Notify the client to download the file
            out.println("SUCESSO: O pdf foi gerado com sucesso. Por favor, faça o download aqui: " + url);

            // Save log for success
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "O pdf gerado por " + username + " foi gerado com sucesso"));
        } catch (Exception e) {
            // Handle exceptions
            out.println("ERRO: Falha ao gerar o relatório PDF.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Falha ao gerar o relatório PDF para " + username));
        }
    }

    private void processExportByTagCommand(TagType tagType, String username, PrintWriter out) {
        try {
            // Generate the endpoint URL
            String url = "http://localhost:8080/download-pdf-report?tag=" + tagType.name();

            // Log the generated URL
            System.out.println("Generated URL for download: " + url);

            // Notify the client to download the file
            out.println("SUCESSO: O pdf foi gerado com sucesso. Por favor, faça o download aqui: " + url);

            // Save log for success
            logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "O pdf gerado por " + username + " foi gerado com sucesso"));
        } catch (Exception e) {
            // Handle exceptions
            out.println("ERRO: Falha ao gerar o relatório PDF.");
            logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Falha ao gerar o relatório PDF para " + username));
        }
    }

    private void processJoinCommand(String username, String name, PrintWriter out) {
        // Buscar o usuário com o nome fornecido
        User user = userService.getUserByName(username); // Método para encontrar o usuário pelo nome de usuário
        if (user == null) {
            out.println("ERRO: Usuário não encontrado");
            return;
        }

        /*TODO: Verificar permissões*/
        if (groupService.isUserInGroup(name, user.getId())) {
            groupService.addUserToGroup(name, user.getId());
        } else {
            System.out.println("utilizador já pertence ao grupo");
        }
        // Buscar o grupo com os parâmetros fornecidos
        Group group = groupService.getUserGroupByName(user.getId(), name); // Método para buscar o grupo
        if (group == null) {
            out.println("ERRO: Grupo não encontrado");
            return;
        }

        // Verificar se o usuário já faz parte do grupo
        if (user.getGroups().contains(group)) {
            out.println("ERRO: Usuário já está no grupo");
            return;
        }

        out.println("SUCESSO: Usuário " + username + " entrou no grupo " + name);

        // Agora, permitir que o usuário entre no chat
        try {
            Chat.startChat(group.getAddress(), Integer.parseInt(group.getPort()), username); // Chama o método para iniciar o chat multicast
        } catch (IOException e) {
            out.println("ERRO: Falha ao tentar entrar no chat: " + e.getMessage());
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
            if (usersWithPermissionsOnline.isEmpty()) {
                pendingApprovals.put(username, operationName);  // Salva a solicitação
                out.println("PENDENTE: Solicitação enviada para aprovação.");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação realizada com sucesso: Solicitação enviada para aprovação."));

                sendNotificationToUserInGroup(username, "PENDENTE: Solicitação enviada para aprovação.", usersWithPermissionsOnline, userService, groupService);
            } else {
                for (String approver : usersWithPermissionsOnline) {
                    notifyUser(approver, "Solicitação para aprovação do comando'" + operationName + "'por" + username, usersWithPermissionsOnline, multicastGroups);
                }
                out.println("PENDENTE: Solicitação enviada para aprovação.");
                logService.saveLog(new Log(LocalDateTime.now(), TagType.SUCCESS, "Operação realizada com sucesso: Solicitação enviada para aprovação."));
            }
        }
    }

    private static void handleApprovalCommand(String action, String username, String requester, PrintWriter out,
                                              Map<String, String> pendingApprovals,
                                              Set<String> usersWithPermissionsOnline,
                                              List<Group> multicastGroups) {
        if (!pendingApprovals.containsKey(requester)) {
            notifyUser(requester, "ERRO: Não há solicitações pendentes para este utilizador.", usersWithPermissionsOnline, multicastGroups);
            out.println("ERRO: Comando desconhecido.");
            return;

        }

        String operationName = pendingApprovals.remove(requester);

        if (action.equals("/approve")) {
            sendNotificationToGroups("Comando executado: " + operationName + " (por " + username + ")", multicastGroups);
            notifyUser(requester, "SUCESSO: Sua solicitação de operação foi aprovada.", usersWithPermissionsOnline, multicastGroups);
            out.println("APPROVE: Aprovado com sucesso");
        } else if (action.equals("/reject")) {
            notifyUser(requester, "ERRO: Sua solicitação de operação foi rejeitada.", usersWithPermissionsOnline, multicastGroups);
            notifyUser(username, "SUCESSO: Operação rejeitada.", usersWithPermissionsOnline, multicastGroups);
            out.println("Reject: Rejectado com sucesso");
        } else {
            notifyUser(username, "ERRO: Comando desconhecido. Use APPROVE ou REJECT.", usersWithPermissionsOnline, multicastGroups);
            out.println("ERRO: Comando desconhecido.");
        }
    }

    public static void saveNotificationForLater(String username, String message) {
        // Aqui você poderia salvar as notificações que não puderam ser enviadas
        // Exemplo: armazenar em uma tabela no banco de dados ou em uma lista temporária
        out.println("Notificação salva para " + username + ": " + message);

        // Exemplo de armazenamento simples em um Map ou Lista
        // Você pode usar uma abordagem diferente dependendo de como deseja salvar as notificações
        pendingApprovals.put(username, message);  // Mapa fictício para armazenar as notificações pendentes
        // Se você estiver usando um banco de dados, faria a inserção aqui
    }
}
