package org.estg.ipp.pt.ClientSide;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Classes.Enums.ServerResponseRegex;
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
                System.out.print(Constants.MENU);

                int choice;
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println(Constants.ERROR_INVALID_MENU_OPTION);
                    scanner.nextLine();
                    continue;
                }

                switch (choice) {
                    case 1 -> handleSignUp(scanner, out, in);
                    case 2 -> handleLogin(scanner, out, in, socket, serverAddress);
                    case 3 -> {
                        System.out.println(Constants.EXITING_APP);
                        keepRunning = false;
                    }
                    default -> System.out.println(Constants.ERROR_INVALID_MENU_OPTION);
                }
            }
        }
    }

    private static void handleSignUp(Scanner scanner, PrintWriter out, BufferedReader in) {
        System.out.print(Constants.INPUT_USER_NAME);
        String username = scanner.nextLine();
        String email;
        while (true) {
            System.out.print(Constants.INPUT_USER_EMAIL);
            email = scanner.nextLine();

            if (ServerResponseRegex.EMAIL.matches(email)) {
                break;
            } else {
                System.out.println(Constants.ERROR_INVALID_EMAIL);
            }
        }

        System.out.print(Constants.INPUT_USER_PASSWORD);
        String password = scanner.nextLine();
        out.println("REGISTER:" + username + "," + email + "," + password);

        try {
            System.out.println(in.readLine());
        } catch (IOException e) {
            System.out.println(Constants.ERROR_SIGN_UP);
        }
    }

    private static void handleLogin(Scanner scanner, PrintWriter out, BufferedReader in, Socket socket, String serverAddress) {
        System.out.print(Constants.INPUT_USER_NAME_EMAIL);
        String usernameOrEmail = scanner.nextLine();
        System.out.print(Constants.INPUT_USER_PASSWORD);
        String password = scanner.nextLine();
        out.println("LOGIN:" + usernameOrEmail + "," + password);

        String response;
        try {
            response = in.readLine();
        } catch (IOException e) {
            System.out.println(Constants.ERROR_LOGIN);
            return;
        }
        System.out.println(response);

        Matcher matcher = ServerResponseRegex.LOGIN_SUCCESS.matcher(response);
        if (matcher.matches()) {
                String groupAddress = matcher.group("address");
                int port = Integer.parseInt(matcher.group("port"));

                try {
                    MulticastChatService chatService = new MulticastChatService(groupAddress, port, usernameOrEmail, socket, serverAddress);

                    chatService.startChat(groupAddress, port, usernameOrEmail);
                } catch (IOException e) {
                    System.out.println(Constants.ERROR_STARTING_CHAT_SESSION);
                }
        } else if (ServerResponseRegex.LOGIN_FAILED.matches(response)) {
            System.out.println(Constants.ERROR_INVALID_CREDENTIALS);
        } else if (!ServerResponseRegex.GENERIC_RESPONSE.matches(response)) {
            System.out.println(Constants.ERROR_GENERIC);
        }
    }
}
