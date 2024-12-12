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

/**
 * Classe principal do servidor que gerencia as conexões com os clientes e executa comandos internos e de usuários.
 * A classe é responsável por gerenciar sockets, executar comandos, e manter as estatísticas do servidor.
 * Além disso, a classe escuta e processa as solicitações recebidas dos clientes, e se comunica com diferentes
 * serviços, como {@link ExecuteInternalCommands}, {@link ExecuteUserCommands}, {@link GroupService},
 * {@link LogService}, e {@link MessageService}.
 *
 * @SpringBootApplication(scanBasePackages = {"org.estg.ipp.pt.ServerSide", "org.estg.ipp.pt.Security"})
 * Esta classe é a aplicação principal do servidor e inicializa os componentes Spring necessários.
 */
@SpringBootApplication(scanBasePackages = {"org.estg.ipp.pt.ServerSide", "org.estg.ipp.pt.Security"})
public class Server {

    /**
     * Componente responsável pela execução de comandos internos do servidor.
     */
    @Autowired
    private ExecuteInternalCommands internalCommands;

    /**
     * Componente responsável pela execução de comandos relacionados aos usuários.
     */
    @Autowired
    private ExecuteUserCommands userCommands;

    /**
     * Serviço responsável pela gestão de grupos.
     */
    @Autowired
    private GroupService groupService;

    /**
     * Mapa para armazenar os utilizadores online e suas permissões.
     */
    public final ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline = new ConcurrentHashMap<>();

    /**
     * Mapa para armazenar as conexões de clientes ativas.
     */
    private static final ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();

    /**
     * Serviço responsável pela gestão de logs no servidor.
     */
    @Autowired
    private LogService logService;

    /**
     * Serviço responsável pela gestão de mensagens no servidor.
     */
    @Autowired
    private MessageService messageService;

    /**
     * Socket do servidor para escutar conexões de clientes.
     */
    private ServerSocket serverSocket;

    /**
     * Método principal que inicia a aplicação Spring Boot para o servidor.
     *
     * @param args Argumentos de linha de comando.
     */
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    /**
     * Bean que inicializa o servidor, configura o serviço multicast, e começa a escutar por conexões de clientes.
     *
     * @param executeInternalCommands Componente para executar comandos internos.
     * @return CommandLineRunner para inicializar o servidor.
     */
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

    /**
     * Recupera o socket de um usuário conectado com base no nome de usuário.
     *
     * @param username Nome do usuário.
     * @return Socket correspondente ao usuário.
     */
    public static Socket getUserSocket(String username) {
        return clients.get(username);
    }

    /**
     * Remove um usuário da lista de conexões ativas e fecha sua conexão.
     *
     * @param username Nome do usuário.
     */
    public static void removeUserSocket(String username) {
        try {
            Socket socket = clients.remove(username);
            if (socket != null) {
                socket.close(); // Fechar a conexão
            }
        } catch (Exception e) {
            System.err.println("Erro ao efetuar logout: " + e.getMessage());
        }
    }

    /**
     * Retorna o número de clientes atualmente conectados ao servidor.
     *
     * @return Número de clientes conectados.
     */
    public static int getNumberOfClients() {
        return clients.size();
    }

    /**
     * Adiciona um novo utilizador e sua conexão ao mapa de conexões.
     *
     * @param username Nome do utilizador.
     * @param socket   Socket do utilizador.
     */
    public static void addUserSocket(String username, Socket socket) {
        clients.put(username, socket);
    }

    /**
     * Método que lida com a comunicação de um cliente conectado ao servidor.
     * Processa as solicitações recebidas, executando comandos internos ou de usuários conforme necessário.
     *
     * @param clientSocket Conexão com o cliente.
     * @param serverStats Estatísticas do servidor.
     */
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

    /**
     * Mostra as estatísticas do servidor a cada minuto.
     *
     * @param serverStats Estatísticas do servidor.
     */
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
