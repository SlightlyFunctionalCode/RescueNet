package org.estg.ipp.pt;

import org.estg.ipp.pt.ClientSide.Classes.Connection;
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

    private static Connection connection;

    public static void main(String[] args) throws IOException {
        connection = new Connection("localhost", 5000);
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
                case 1 -> handleSignUp(scanner);
                case 2 -> handleLogin(scanner);
                case 3 -> {
                    System.out.println(Constants.EXITING_APP);
                    keepRunning = false;
                }
                default -> System.out.println(Constants.ERROR_INVALID_MENU_OPTION);
            }
        }
    }

    private static void handleSignUp(Scanner scanner) {
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
        connection.getOut().println("REGISTER:" + username + "," + email + "," + password);

        try {
            System.out.println(connection.getIn().readLine());
        } catch (IOException e) {
            System.out.println(Constants.ERROR_SIGN_UP);
        }
    }

    private static void handleLogin(Scanner scanner) {
        connection.reconnect();

        System.out.print(Constants.INPUT_USER_NAME_EMAIL);
        String usernameOrEmail = scanner.nextLine();
        System.out.print(Constants.INPUT_USER_PASSWORD);
        String password = scanner.nextLine();
        connection.getOut().println("LOGIN:" + usernameOrEmail + "," + password);

        String response;
        try {
            response = connection.getIn().readLine();
        } catch (IOException e) {
            System.out.println(Constants.ERROR_LOGIN);
            return;
        }
        System.out.println(response);

        Matcher matcher = ServerResponseRegex.LOGIN_SUCCESS.matcher(response);
        if (matcher.matches()) {
            String groupAddress = matcher.group("address");
            System.out.println(groupAddress);
            int port = Integer.parseInt(matcher.group("port"));
            System.out.println(port);
            try {
                MulticastChatService chatService = new MulticastChatService(groupAddress, port, usernameOrEmail, connection.getSocket(), "localhost");
                System.out.println(chatService);
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
