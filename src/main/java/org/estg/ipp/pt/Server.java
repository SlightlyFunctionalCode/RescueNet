package org.estg.ipp.pt;


import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.User;
import org.estg.ipp.pt.Services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.SQLOutput;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Server{

    private static final Set<String> loggedUsers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Server.class, args);
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
        payload = payload.trim();
        String[] parts = payload.split(",",3);
        if (parts.length != 3) {
            return "ERRO: Formato inválido para registro";
        }
        String username = parts[0];
        String email = parts[1];
        String password = parts[2];


        System.out.println(email);
        User user = new User();
        UserService userService = new UserService();
        user.setName(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPermissions(Permissions.LOW_LEVEL);

        try {
            if(userService.register(user) == 0){
                return "FAILED: Usuário com nome ou email já existente";
            }else{
                return "SUCESSO: Usuário registrado com sucesso";
            }

        } catch (Exception e) {
            return "ERRO: Falha ao registrar usuário - " + e.getMessage();
        }

    }

    private static String loginUser(String payload) {
        String[] parts = payload.split(",");
        if (parts.length != 2) return "ERRO: Formato inválido para login";

        String usernameOremail = parts[0];
        String password = parts[1];

        UserService userService = new UserService();
        User user;
        user = userService.authenticate(usernameOremail, password);

        if(user == null){
            return "FAILED: User invalid!";
        }

        //Atribuir grupo com base nas permissões
        String groupAddress;
        int port;
        switch (user.getPermissions()) {
            case LOW_LEVEL -> {
                groupAddress = "230.0.0.1";
                port = 4446;
            }
            case MEDIUM_LEVEL -> {
                groupAddress = "230.0.0.2";
                port = 4447;
            }
            case HIGH_LEVEL -> {
                groupAddress = "230.0.0.3";
                port = 4448;
            }
            default -> {
                return "ERRO: Permissão desconhecida";
            }

        }
        return "SUCESSO: Login realizado. Grupo: " + groupAddress + ":" + port;
    }

    private static String logoutUser(String username) {

        return null;
    }
}


