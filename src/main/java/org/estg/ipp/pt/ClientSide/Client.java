package org.estg.ipp.pt.ClientSide;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Classes.MulticastChatService;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;

@SpringBootApplication
public class Client {

    public static void main(String[] args) throws IOException {
        String serverAddress = "localhost";
        int serverPort = 5000;

        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            Scanner scanner = new Scanner(System.in);
            boolean keepRunning = true;
            while (keepRunning) {
                System.out.println("\nMENU");
                System.out.println("1. Registar");
                System.out.println("2. Login");
                System.out.println("3. Sair");
                System.out.print("Escolha uma opção: ");

                int choice;
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Por favor, insira uma opção válido.");
                    scanner.nextLine();
                    continue;
                }

                switch (choice) {
                    case 1 -> handleSignUp(scanner, out, in);
                    case 2 -> handleLogin(scanner, out, in, socket, serverAddress);
                    case 3 -> {
                        System.out.println("Encerrando cliente...");
                        keepRunning = false;
                    }
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            }
        }
    }

    private static void handleSignUp(Scanner scanner, PrintWriter out, BufferedReader in) {
        System.out.print("Digite o nome de utilizador: ");
        String username = scanner.nextLine();
        String email;
        while (true) {
            System.out.print("Digite um email: ");
            email = scanner.nextLine();

            if (RegexPatterns.EMAIL.matches(email)) {
                break;
            } else {
                System.out.println("Email inválido. Certifique-se de que contém '@' e '.' após o '@'. Tente novamente.");
            }
        }

        System.out.print("Digite a palavra-passe: ");
        String password = scanner.nextLine();
        out.println("REGISTER:" + username + "," + email + "," + password);

        try {
            System.out.println(in.readLine());
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao efetuar o Registo");
        }
    }

    private static void handleLogin(Scanner scanner, PrintWriter out, BufferedReader in, Socket socket, String serverAddress) {
        System.out.print("Digite o nome de utilizador/email: ");
        String usernameOrEmail = scanner.nextLine();
        System.out.print("Digite a senha: ");
        String password = scanner.nextLine();
        out.println("LOGIN:" + usernameOrEmail + "," + password);

        String response;
        try {
            response = in.readLine();
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao efetuar o Login");
            return;
        }
        System.out.println(response);

        if (RegexPatterns.LOGIN_SUCCESS.matches(response)) {
            Matcher matcher = RegexPatterns.LOGIN_SUCCESS.matcher(response);
            if (matcher.find()) {
                String groupAddress = matcher.group(1);
                int port = Integer.parseInt(matcher.group(2));

                try {
                    MulticastChatService chatService = new MulticastChatService(groupAddress, port, usernameOrEmail, socket, serverAddress);


                    chatService.startChat(groupAddress, port, usernameOrEmail);
                } catch (IOException e) {
                    System.out.println("Ocorreu um erro ao iniciar o chat");
                }
            }
        } else if (RegexPatterns.LOGIN_FAILED.matches(response)) {
            System.out.println("Falha ao iniciar sessão. Verifique suas credenciais.");
        } else if (!RegexPatterns.GENERIC_RESPONSE.matches(response)) {
            System.out.println("ERRO: Ocorreu um erro");
        }
    }
}


