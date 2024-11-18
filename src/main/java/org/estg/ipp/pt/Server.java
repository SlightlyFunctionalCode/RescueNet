package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Services.Operation;
import org.estg.ipp.pt.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.*;

import static java.lang.System.out;
import static org.estg.ipp.pt.Notifications.*;

@SpringBootApplication(scanBasePackages = "org.estg.ipp.pt") // Ensures the base package is correct
public class Server {

    private static final Set<String> usersWithPermissionsOnline = new HashSet<>();
    private static final Map<String, Socket> userSockets = new HashMap<>();
    private static final Map<String, String> pendingApprovals = new HashMap<>();

    private static final List<AbstractMap.SimpleEntry<String, Integer>> multicastGroups = List.of(
            new AbstractMap.SimpleEntry<>("230.0.0.1", 4446), // LOW_LEVEL
            new AbstractMap.SimpleEntry<>("230.0.0.2", 4447), // MEDIUM_LEVEL
            new AbstractMap.SimpleEntry<>("230.0.0.3", 4448)  // HIGH_LEVEL
    );


    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args); // Start the Spring Boot application
    }

    // This method will be executed after Spring Boot initializes the application context
    @Bean
    public CommandLineRunner startServer() {
        return args -> {
            int serverPort = 5000;
            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                out.println("Servidor iniciado na porta " + serverPort);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    out.println("Cliente conectado: " + clientSocket.getInetAddress());

                    new Thread(() -> handleClient(clientSocket)).start(); // Handle client in a new thread
                }
            } catch (IOException e) {
                System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            }
        };
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Solicitação recebida: " + request);
                String[] parts = request.split(":", 2);
                String command = parts[0];
                String[] mini_parts = command.split( " ", 2);
                String command2 = mini_parts.length == 2 ? mini_parts[0]: "";
                String requester = mini_parts.length == 2 ? mini_parts[1]: "";
                String payload = parts.length > 1 ? parts[1] : "";
                if(!command2.isEmpty()){
                    command = command2;
                    payload = command + ":" + payload;
                }

                switch (command) {
                    case "REGISTER" -> out.println(registerUser(payload));
                    case "LOGIN" -> {String response = handleLogin(payload, out, clientSocket);
                        out.println(response);
                        if (response.startsWith("SUCESSO")) {
                            // Usuário autenticado com sucesso
                            String username = payload.split(",")[0]; // Obtém o nome de usuário
                            User user = userService.getUserByName(username);
                            System.out.println("ola");
                            if (user != null && (user.getPermissions() == Permissions.HIGH_LEVEL || user.getPermissions() == Permissions.MEDIUM_LEVEL)) {
                                usersWithPermissionsOnline.add(username);
                                System.out.println("ola2");
                                // Enviar notificações de pedidos pendentes
                                for (Map.Entry<String, String> entry : pendingApprovals.entrySet()) {
                                    System.out.println("ola1");
                                    String requestingUser = entry.getKey();
                                    String operationName = entry.getValue();
                                    notifyUser(username, "Pedido pendente: O usuário " + requestingUser + " solicitou a operação '" + operationName + "'. Aprove ou rejeite.", usersWithPermissionsOnline, multicastGroups);
                                }
                            }

                        }
                    }
                    case "LOGOUT" -> out.println(logoutUser(payload));
                    case "MASS_EVACUATION", "RESOURCE_DISTRIBUTION", "EMERGENCY_COMM" -> {
                        // Processar comandos de operações
                        processOperationCommand(payload, command, out);
                    }
                    case "APPROVE", "REJECT" -> handleApprovalCommand(payload, requester, out);
                    default -> out.println("ERRO: Comando inválido");
                }

            }
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
        }
    }


    private String handleLogin(String payload, PrintWriter out, Socket clientSocket) {
        String response = loginUser(payload);
        out.println(response);
        if (response.startsWith("SUCESSO")) {
            // Após o login bem-sucedido, armazena o socket do usuário
            String username = payload.split(",")[0]; // Obtém o nome de usuário
            userSockets.put(username, clientSocket);  // Armazena o socket
        }
        return response;
    }

    private String registerUser(String payload) {
        payload = payload.trim();
        String[] parts = payload.split(",", 3);
        if (parts.length != 3) {
            return "ERRO: Formato inválido para registro";
        }
        String username = parts[0];
        String email = parts[1];
        String password = parts[2];

        out.println("Tentando registrar o usuário com email: " + email);
        User user = new User();
        user.setName(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPermissions(Permissions.LOW_LEVEL);

        try {
            if (userService.register(user) == 0) {
                return "FAILED: Usuário com nome ou email já existente";
            } else {
                return "SUCESSO: Usuário registrado com sucesso";
            }
        } catch (Exception e) {
            return "ERRO: Falha ao registrar usuário - " + e.getMessage();
        }
    }

    private String loginUser(String payload) {
        String[] parts = payload.split(",");
        if (parts.length != 2) return "ERRO: Formato inválido para login";

        String usernameOrEmail = parts[0];
        String password = parts[1];

        User user = userService.authenticate(usernameOrEmail, password);

        if (user == null) {
            return "FAILED: Usuário inválido!";
        }

        //Atribuir grupo com base nas permissões
        String group = getGroupAddressAndPort(user);
        String groupAddress;
        String port;
        String[] group_parts = group.split(":", 2);
        groupAddress = group_parts[0];
        port = group_parts[1];

        return "SUCESSO: Login realizado. Grupo: " + groupAddress + ":" + port;
    }

    protected static String getGroupAddressAndPort(User user){
        String groupAddress;
        int port;
        switch (user.getPermissions()) {
            case LOW_LEVEL:
                groupAddress = "230.0.0.1";
                port = 4446;
                break;
            case MEDIUM_LEVEL:
                groupAddress = "230.0.0.2";
                port = 4447;
                break;
            case HIGH_LEVEL:
                groupAddress = "230.0.0.3";
                port = 4448;
                break;
            default:
                return "ERRO: Permissão desconhecida";
        }
        return groupAddress + ":" + port;
    }

    private static String logoutUser(String username) {
        return null;
    }

    protected static Socket getUserSocket(String username) {
        Socket socket = userSockets.get(username);
        if (socket == null) {
            out.println("Erro: Socket do usuário " + username + " não encontrado.");
        }
        return socket;
    }

    protected static void saveNotificationForLater(String username, String message) {
        // Aqui você poderia salvar as notificações que não puderam ser enviadas
        // Exemplo: armazenar em uma tabela no banco de dados ou em uma lista temporária
        out.println("Notificação salva para " + username + ": " + message);

        // Exemplo de armazenamento simples em um Map ou Lista
        // Você pode usar uma abordagem diferente dependendo de como deseja salvar as notificações
        pendingApprovals.put(username, message);  // Mapa fictício para armazenar as notificações pendentes
        // Se você estiver usando um banco de dados, faria a inserção aqui
    }

    private void processOperationCommand(String username, String operationName, PrintWriter out) {
        User user = userService.getUserByName(username);
        if (user == null) {
            out.println("ERRO: Utilizador não encontrado.");
            return;
        }

        Operation operation = switch (operationName.toUpperCase()) {
            case "MASS_EVACUATION" -> new Operation("Operação de evacuação em massa", Permissions.HIGH_LEVEL);
            case "RESOURCE_DISTRIBUTION" -> new Operation("Distribuição de Recursos de Emergência", Permissions.LOW_LEVEL);
            case "EMERGENCY_COMM" -> new Operation("Ativação de comunicações de Emergência", Permissions.MEDIUM_LEVEL);
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
            if(usersWithPermissionsOnline.isEmpty()){
                pendingApprovals.put(username, operationName);  // Salva a solicitação
                out.println("PENDENTE: Solicitação enviada para aprovação.");
                sendNotificationToUserInGroup(username, "PENDENTE: Solicitação enviada para aprovação.", usersWithPermissionsOnline, userService);
            }else{
                for (String approver : usersWithPermissionsOnline) {
                    notifyUser(approver, "Solicitação para aprovação do comando'" + operationName + "'por" + username, usersWithPermissionsOnline, multicastGroups);
                }
                out.println("PENDENTE: Solicitação enviada para aprovação.");
            }
        }
    }

    private static void handleApprovalCommand(String payload, String requester, PrintWriter out) {
        System.out.println(payload);
        String[] parts = payload.split(":", 2);
        String action = parts[0];
        String username = parts[1];

        if (!pendingApprovals.containsKey(requester)) {
            notifyUser(requester, "ERRO: Não há solicitações pendentes para este utilizador.", usersWithPermissionsOnline, multicastGroups);
            out.println("ERRO: Comando desconhecido.");
            return;

        }

        String operationName = pendingApprovals.remove(requester);

        if (action.equals("APPROVE")) {
            sendNotificationToGroups("Comando executado: "  + operationName + " (por " + username + ")", multicastGroups);
            notifyUser(requester, "SUCESSO: Sua solicitação de operação foi aprovada.", usersWithPermissionsOnline, multicastGroups);
            out.println("APPROVE: Aprovado com sucesso");
        } else if (action.equals("REJECT")) {
            notifyUser(requester, "ERRO: Sua solicitação de operação foi rejeitada.", usersWithPermissionsOnline, multicastGroups);
            notifyUser(username, "SUCESSO: Operação rejeitada.", usersWithPermissionsOnline, multicastGroups);
            out.println("Reject: Rejectado com sucesso");
        } else {
            notifyUser(username, "ERRO: Comando desconhecido. Use APPROVE ou REJECT.", usersWithPermissionsOnline, multicastGroups);
            out.println("ERRO: Comando desconhecido.");
        }
    }
}
