package org.estg.ipp.pt.ClientSide;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Classes.DefaultCommandHandler;
import org.estg.ipp.pt.ClientSide.Classes.DefaultMessageHandler;
import org.estg.ipp.pt.ClientSide.Classes.MulticastChatService;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;
import org.estg.ipp.pt.Services.MulticastManagerService;
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
        //String serverAddress = "26.253.125.100";
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
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consumir quebra de linha

                switch (choice) {
                    case 1 -> {
                        System.out.print("Digite o nome de utilizador: ");
                        String username = scanner.nextLine();
                        String email;
                        while (true) {
                            System.out.print("Digite um email: ");
                            email = scanner.nextLine();

                            if (RegexPatterns.EMAIL.matches(email)) {
                                break; // Email válido, sai do loop
                            } else {
                                System.out.println("Email inválido. Certifique-se de que contém '@' e '.' após o '@'. Tente novamente.");
                            }
                        }
                        System.out.print("Digite a palavra-passe: ");
                        String password = scanner.nextLine();
                        out.println("REGISTER:" + username + "," + email + "," + password);
                        System.out.println(in.readLine());
                    }
                    case 2 -> {
                        System.out.print("Digite o nome de utilizador/email: ");
                        String usernameOrEmail = scanner.nextLine();
                        System.out.print("Digite a senha: ");
                        String password = scanner.nextLine();
                        out.println("LOGIN:" + usernameOrEmail + "," + password);

                        String response = in.readLine();
                        System.out.println(response);

                        if (RegexPatterns.LOGIN_SUCCESS.matches(response)) {
                            Matcher matcher = RegexPatterns.LOGIN_SUCCESS.matcher(response);
                            if (matcher.find()) {
                                String groupAddress = matcher.group(1);
                                int port = Integer.parseInt(matcher.group(2));

                                MessageHandler messageHandler = new DefaultMessageHandler(null);
                                CommandHandler commandHandler = new DefaultCommandHandler(null, serverAddress);
                                MulticastManagerService multicastManager = MulticastManagerService.getInstance();
                                MulticastChatService chatService = new MulticastChatService(groupAddress, port, usernameOrEmail, multicastManager, commandHandler, messageHandler);

                                chatService.startChat(groupAddress, port, usernameOrEmail);
                            }
                        } else if (RegexPatterns.LOGIN_FAILED.matches(response)) {
                            System.out.println("Falha ao iniciar sessão. Verifique suas credenciais.");
                        } else if (!RegexPatterns.GENERIC_RESPONSE.matches(response)) {
                            System.out.println("ERROR: Something went wrong");
                        }
                    }
                    case 3 -> {
                        System.out.println("Encerrando cliente...");
                        return;
                    }
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            }
        }
    }
}


