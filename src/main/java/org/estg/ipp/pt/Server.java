package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Services.Operation;
import org.estg.ipp.pt.Services.UserService;
import org.estg.ipp.pt.Services.*;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.*;
import java.util.regex.Matcher;

import static java.lang.System.out;
import static org.estg.ipp.pt.Notifications.*;

@SpringBootApplication(scanBasePackages = "org.estg.ipp.pt")
public class Server {

    @Autowired
    private ExecuteInternalCommands internalCommands;

    @Autowired
    private ExecuteUserCommands userCommands;


    public final Map<String, String> pendingApprovals = new HashMap<>();
    public final Set<String> usersWithPermissionsOnline = new HashSet<>();
    public static final Map<String, Socket> userSockets = new HashMap<>();

    @Autowired
    private LogService logService;

    private ServerSocket serverSocket;

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args); // Start the Spring Boot application
    }

    @Bean
    public CommandLineRunner startServer(ExecuteInternalCommands executeInternalCommands) {
        return args -> {
            int serverPort = 5000;

            executeInternalCommands.groupService.initializeDefaultGroups();
            executeInternalCommands.userService.initializeUser();

            try {
                serverSocket = new ServerSocket(serverPort);
                System.out.println("Servidor iniciado na porta " + serverPort);
                logService.saveLog(new Log(LocalDateTime.now(), TagType.INFO, "Servidor iniciado na porta " + serverPort));

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ACCESS, "Cliente conectado: " + clientSocket.getInetAddress()));

                    // Usar threads para lidar com clientes
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
                logService.saveLog(new Log(LocalDateTime.now(), TagType.CRITICAL, "Erro ao iniciar o servidor: " + e.getMessage()));
            }
        };
    }



    // Method to retrieve user socket by username
    public static Socket getUserSocket(String username) {
        return userSockets.get(username);
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Solicitação recebida: " + request);
                logService.saveLog(new Log(LocalDateTime.now(), TagType.USER_ACTION, "Solicitação recebida: " + request));

                Matcher requestMatcher = RegexPatternsCommands.REQUEST.matcher(request);
                System.out.println(requestMatcher);
                if (requestMatcher.matches()) {
                    String command = requestMatcher.group("command");
                    String requester = requestMatcher.group("requester");
                    String payload = requestMatcher.group("payload") != null ? requestMatcher.group("payload") : "";
                    System.out.println(command);
                    System.out.println(payload);
                    // Delegar o comando à classe correta
                    if (internalCommands.isInternalCommand(command)) {
                        internalCommands.handleInternalCommand(command, payload, out, clientSocket, userCommands.groupService.getAllGroups(), usersWithPermissionsOnline, pendingApprovals);
                    } else {
                        userCommands.handleUserCommand(
                                serverSocket.getInetAddress(), command, request, requester, payload, out,
                                pendingApprovals, usersWithPermissionsOnline);
                    }
                } else {
                    out.println("ERRO: Formato de solicitação inválido");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato de solicitação inválido"));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
            logService.saveLog(new Log(LocalDateTime.now(), TagType.FAILURE, "Erro ao comunicar com o cliente: " + e.getMessage()));
        }
    }
}
