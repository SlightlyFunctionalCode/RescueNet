package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.RegexPatternsCommands;
import org.estg.ipp.pt.Classes.Enum.TagType;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.Classes.Log;
import org.estg.ipp.pt.ServerSide.Classes.MulticastListener;
import org.estg.ipp.pt.ServerSide.Classes.ExecuteUserCommandsImpl;
import org.estg.ipp.pt.ServerSide.Classes.ServerStats;
import org.estg.ipp.pt.ServerSide.Interfaces.ExecuteInternalCommands;
import org.estg.ipp.pt.ServerSide.Interfaces.ExecuteUserCommands;
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
 * Classe principal do servidor que gere as conexões com os clientes e executa os comandos internos e os de utilizadores.
 * A classe é responsável por gerir os ‘sockets’, executar os comandos, e manter as estatísticas do servidor, através de
 * classes auxiliares, como o {@link ExecuteInternalCommands} e o {@link ExecuteInternalCommands}.
 * Também contém instâncias do {@link GroupService}, do {@link LogService}, do {@link UserService}, e do {@link MessageService},
 * para gerir os grupos, logs, utilizadores e mensagens, respetivamente.
 *
 * <p>Esta classe é também responsável por encontrar e inicializar os componentes Spring necessários.</p>
 */
@SpringBootApplication(scanBasePackages = {"org.estg.ipp.pt.ServerSide", "org.estg.ipp.pt.Security"})
public class Server {

    /**
     * Componente responsável pela execução de comandos internos do servidor.
     */
    @Autowired
    private ExecuteInternalCommands internalCommands;

    /**
     * Componente responsável pela execução de comandos relacionados aos utilizadores.
     */
    @Autowired
    private ExecuteUserCommands userCommands;

    /**
     * Serviço responsável pela gestão de grupos.
     */
    @Autowired
    private GroupService groupService;

    /**
     * Serviço responsável pela gestão de users.
     */
    @Autowired
    private UserService userService;

    /**
     * Mapa para armazenar os utilizadores online e as suas permissões.
     */
    public final ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline = new ConcurrentHashMap<>();

    /**
     * Mapa para armazenar as conexões de clientes ativas, com o nome do mesmo e a socket
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
     * @return CommandLineRunner para inicializar o servidor.
     */
    @Bean
    public CommandLineRunner startServer() {
        return args -> {
            int serverPort = 5000;

            ServerStats serverStats = new ServerStats();

            groupService.initializeDefaultGroups();
            userService.initializeUser();

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
     * Recupera o ‘socket’ de um utilizador conectado com base no nome do mesmo.
     *
     * @param username Nome do utilizador.
     * @return ‘Socket’ correspondente ao utilizador.
     */
    public static Socket getUserSocket(String username) {
        return clients.get(username);
    }

    /**
     * Remove um utilizador da lista de conexões ativas e fecha a sua conexão.
     *
     * @param username Nome do utilizador.
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
     * Devolve o número de clientes atualmente conectados ao servidor.
     *
     * @return Número de clientes conectados.
     */
    public static int getNumberOfClients() {
        return clients.size();
    }

    /**
     * Adiciona um novo utilizador e a sua conexão ao mapa de conexões.
     *
     * @param username Nome do utilizador.
     * @param socket   Socket do utilizador.
     */
    public static void addUserSocket(String username, Socket socket) {
        clients.put(username, socket);
    }

    /**
     * Método que lida com a comunicação de um cliente conectado ao servidor.
     * Processa as solicitações recebidas e executa comandos internos ou de utilizador conforme necessário.
     *
     * @param clientSocket Conexão com o cliente.
     * @param serverStats  Estatísticas do servidor.
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
     * Mostra as estatísticas do servidor a cada minuto. Nomeadamente o número de utilizadores conectados e o número de
     * comandos utilizados.
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
