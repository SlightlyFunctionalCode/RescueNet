package org.estg.ipp.pt;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.User;
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

@SpringBootApplication(scanBasePackages = "org.estg.ipp.pt") // Ensures the base package is correct
public class Server {

    private static final Set<String> loggedUsers = new HashSet<>();

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
                System.out.println("Servidor iniciado na porta " + serverPort);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

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
                String payload = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "REGISTER":
                        out.println(registerUser(payload));
                        break;
                    case "LOGIN":
                        out.println(loginUser(payload));
                        break;
                    case "LOGOUT":
                        out.println(logoutUser(payload));
                        break;
                    default:
                        out.println("ERRO: Comando inválido");
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o cliente: " + e.getMessage());
        }
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

        System.out.println("Tentando registrar o usuário com email: " + email);
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

        // Atribuir grupo com base nas permissões
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
        return "SUCESSO: Login realizado. Grupo: " + groupAddress + ":" + port;
    }

    private String logoutUser(String username) {
        // Add logic to handle logout
        loggedUsers.remove(username);
        return "SUCESSO: Usuário " + username + " desconectado.";
    }
}
