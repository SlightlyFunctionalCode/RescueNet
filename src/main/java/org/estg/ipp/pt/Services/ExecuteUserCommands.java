package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.Classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static java.lang.System.out;
import static org.estg.ipp.pt.Notifications.*;
import static org.estg.ipp.pt.Notifications.notifyUser;
import static org.estg.ipp.pt.Server.pendingApprovals;

@Component
public class ExecuteUserCommands {
    @Autowired
    private UserService userService;

    public void handleUserCommand(String command, String request, String requester, String payload, PrintWriter out,
                                  Map<String, String> pendingApprovals,
                                  Set<String> usersWithPermissionsOnline, List<AbstractMap.SimpleEntry<String, Integer>> multicastGroups) {
        switch (command) {
            case "/evac", "/resdist", "/emerg" -> processOperationCommand(payload, command, out, pendingApprovals, usersWithPermissionsOnline, multicastGroups);
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
            default -> out.println("ERRO: Comando de utilizador inválido");
        }
    }

    private void processOperationCommand(String username, String operationName, PrintWriter out,
                                         Map<String, String> pendingApprovals,
                                         Set<String> usersWithPermissionsOnline, List<AbstractMap.SimpleEntry<String, Integer>> multicastGroups) {
        User user = userService.getUserByName(username);
        if (user == null) {
            out.println("ERRO: Utilizador não encontrado.");
            return;
        }

        Operation operation = switch (operationName) {
            case "/evac" -> new Operation("Operação de evacuação em massa", Permissions.HIGH_LEVEL);
            case "/resdist" ->
                    new Operation("Distribuição de Recursos de Emergência", Permissions.LOW_LEVEL);
            case "/emerg" -> new Operation("Ativação de comunicações de Emergência", Permissions.MEDIUM_LEVEL);
            default -> null;
        };

        if (operation == null) {
            out.println("ERRO: Operação desconhecida.");
            return;
        }

        if (user.getPermissions().ordinal() >= operation.getRequiredPermission().ordinal()) {
            // Permissão suficiente - executar a operação
            sendNotificationToGroups("Comando executado: " + operation.getName() + " (por " + username + ")", multicastGroups);
            out.println("SUCESSO: Operação realizada.");
        } else {
            if (usersWithPermissionsOnline.isEmpty()) {
                pendingApprovals.put(username, operationName);  // Salva a solicitação
                out.println("PENDENTE: Solicitação enviada para aprovação.");
                sendNotificationToUserInGroup(username, "PENDENTE: Solicitação enviada para aprovação.", usersWithPermissionsOnline, userService);
            } else {
                for (String approver : usersWithPermissionsOnline) {
                    notifyUser(approver, "Solicitação para aprovação do comando'" + operationName + "'por" + username, usersWithPermissionsOnline, multicastGroups);
                }
                out.println("PENDENTE: Solicitação enviada para aprovação.");
            }
        }
    }

    private static void handleApprovalCommand(String action, String username, String requester, PrintWriter out,
                                              Map<String, String> pendingApprovals,
                                              Set<String> usersWithPermissionsOnline,
                                              List<AbstractMap.SimpleEntry<String, Integer>> multicastGroups) {
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
