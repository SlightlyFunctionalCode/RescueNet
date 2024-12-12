package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.ServerSide.Classes.MulticastListener;
import org.estg.ipp.pt.ServerSide.Classes.ExecuteInternalCommands;
import org.estg.ipp.pt.ServerSide.Classes.ExecuteUserCommands;
import org.estg.ipp.pt.ServerSide.Classes.ServerStats;
import org.estg.ipp.pt.ServerSide.Services.*;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

@SpringBootApplication(scanBasePackages = {"org.estg.ipp.pt.ServerSide", "org.estg.ipp.pt.Security"})
public class Server {

    @Autowired
    private ExecuteInternalCommands internalCommands;

    @Autowired
    private ExecuteUserCommands userCommands;

    @Autowired
    private GroupService groupService;

    public final ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();
    @Autowired
    private LogService logService;

    @Autowired
    private MessageService messageService;

    private ServerSocket serverSocket;

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    @Bean
    public CommandLineRunner startServer(ExecuteInternalCommands executeInternalCommands) {
        return args -> {
            int serverPort = 5000;

            ServerStats serverStats = new ServerStats();

            executeInternalCommands.groupService.initializeDefaultGroups();
            executeInternalCommands.userService.initializeUser();

            try {
                serverSocket = new ServerSocket(serverPort);
                System.out.println("Servidor iniciado na porta " + serverPort);
                logService.saveLog(new Log(LocalDateTime.now(), TagType.INFO, "Servidor iniciado na porta " + serverPort));

                for (Group g : groupService.getAllGroups()) {
                    MulticastListener handleMulticastMessages = new MulticastListener();
                    new Thread(() -> handleMulticastMessages.handleMulticastMessages(g, "localhost", messageService)).start();
                }

                printReport(serverStats);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ACCESS, "Cliente conectado: " + clientSocket.getInetAddress()));

                    new Thread(() -> handleClient(clientSocket, serverStats)).start();
                }
            } catch (IOException e) {
                System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
                logService.saveLog(new Log(LocalDateTime.now(), TagType.CRITICAL, "Erro ao iniciar o servidor: " + e.getMessage()));
            }
        };
    }

    public static Socket getUserSocket(String username) {
        return clients.get(username);
    }

    public static void removeUserSocket(String username) {
        try {
            clients.remove(username);
        } catch (Exception e) {
            System.err.println("Erro ao efetuar logout: " + e.getMessage());
        }
    }

    public static int getNumberOfClients() {
        return clients.size();
    }

    public static void addUserSocket(String username, Socket socket) {
        clients.put(username, socket);
    }

    private void handleClient(Socket clientSocket, ServerStats serverStats) {
        String user = null;
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Solicitação recebida: " + request);
                logService.saveLog(new Log(LocalDateTime.now(), TagType.USER_ACTION, "Solicitação recebida: " + request));

                Matcher requestMatcher = RegexPatternsCommands.REQUEST.matcher(request);
                if (requestMatcher.matches()) {
                    String command = requestMatcher.group("command");
                    String requester = requestMatcher.group("requester");
                    String payload = requestMatcher.group("payload") != null ? requestMatcher.group("payload") : "";
                    user = payload;
                    System.out.println(user);

                    if (internalCommands.isInternalCommand(command)) {
                        internalCommands.handleInternalCommand(command, payload, out, clientSocket, groupService.getAllGroups(), usersWithPermissionsOnline);
                        serverStats.incrementCommandsExecuted();
                    } else {
                        try {
                            userCommands.handleUserCommand(
                                    command, request, requester, payload, out,
                                    usersWithPermissionsOnline);
                            serverStats.incrementCommandsExecuted();
                        } catch (IOException e) {
                            throw new IOException(e.getMessage());
                        }
                    }
                } else {
                    out.println("ERRO: Formato de solicitação inválido");
                    logService.saveLog(new Log(LocalDateTime.now(), TagType.ERROR, "Formato de solicitação inválido"));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
            System.out.println("teste");
            if (user != null) {
                removeUserSocket(user);
            }
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.err.println("Erro ao fechar o socket: " + ex.getMessage());
            }
            logService.saveLog(new Log(LocalDateTime.now(), TagType.FAILURE, "Erro ao comunicar com o cliente: " + e.getMessage()));
        }
    }

    private void printReport(ServerStats serverStats) {
        new Thread(() -> {
            while (true) {
                try {
                    int connectedUsers = serverStats.getConnectedUsers();
                    int commandsExecuted = serverStats.getTotalCommandsExecuted();

                    System.out.println("===== Estatísticas do Servidor =====");
                    System.out.println("Utilizadores Conectados: " + connectedUsers);
                    System.out.println("Comandos Executados: " + commandsExecuted);
                    System.out.println("====================================");

                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    System.err.println("Thread de estatísticas foi interrompida: " + e.getMessage());
                    break;
                }
            }
        }).start();
    }
}
