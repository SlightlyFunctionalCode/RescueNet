package org.estg.ipp.pt;


import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Services.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {

    private static final Map<String, String> userDatabase = new HashMap<>();
    private static final Set<String> loggedUsers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        int serverPort = 5000; // Porta do servidor
        ServerSocket serverSocket = new ServerSocket(serverPort);

        System.out.println("Servidor iniciado na porta " + serverPort);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
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
                    case "REGISTER" -> out.println(registerUser(payload));
                    case "LOGIN" -> out.println(loginUser(payload));
                    case "LOGOUT" -> out.println(logoutUser(payload));
                    default -> out.println("ERRO: Comando inválido");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao comunicar com o cliente: " + e.getMessage());
        }
    }

    private static String registerUser(String payload) {
        String[] parts = payload.split(",");
        if (parts.length != 2) return "ERRO: Formato inválido para registro";

        String username = parts[0];
        String password = parts[1];

        User user = new User();
        UserService userService = new UserService();
        user.setName(username);
        user.setPassword(password);
        user.setIdentifier("teste");
        user.setProfile("teste");

        try {
            userService.register(user);
            return "SUCESSO: Usuário registrado com sucesso";
        } catch (Exception e) {
            return "ERRO: Falha ao registrar usuário - " + e.getMessage();
        }

    }

    private static String loginUser(String payload) {
        String[] parts = payload.split(",");
        if (parts.length != 2) return "ERRO: Formato inválido para login";

        String username = parts[0];
        String password = parts[1];




        return "SUCESSO: Login realizado";
    }

    private static String logoutUser(String username) {
        if (!loggedUsers.contains(username)) {
            return "ERRO: Usuário não está logado";
        }

        loggedUsers.remove(username);
        return "SUCESSO: Logout realizado";
    }
}


