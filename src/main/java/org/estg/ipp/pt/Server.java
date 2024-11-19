package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
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

    public static final Map<String, String> pendingApprovals = new HashMap<>();
    public static final Set<String> usersWithPermissionsOnline = new HashSet<>();
    public static final List<AbstractMap.SimpleEntry<String, Integer>> multicastGroups = List.of(
            new AbstractMap.SimpleEntry<>("230.0.0.1", 4446), // LOW_LEVEL
            new AbstractMap.SimpleEntry<>("230.0.0.2", 4447), // MEDIUM_LEVEL
            new AbstractMap.SimpleEntry<>("230.0.0.3", 4448)  // HIGH_LEVEL
    );
    public static final Map<String, Socket> userSockets = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args); // Start the Spring Boot application
    }

    @Bean
    public CommandLineRunner startServer() {
        return args -> {
            int serverPort = 5000;


            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Servidor iniciado na porta " + serverPort);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                    // Usar threads para lidar com clientes
                    new Thread(() -> handleClient(clientSocket)).start();
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
                        internalCommands.handleInternalCommand(command, payload, out, clientSocket);
                    } else {
                        userCommands.handleUserCommand(
                                command, request, requester, payload, out,
                                pendingApprovals, usersWithPermissionsOnline, multicastGroups
                        );
                    }
                } else {
                    out.println("ERRO: Formato de solicitação inválido");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
        }
    }
}
