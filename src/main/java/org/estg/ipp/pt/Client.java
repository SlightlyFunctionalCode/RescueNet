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

/**
 * Classe principal do cliente, responsável pela interação com o utilizador e comunicação com o servidor.
 *
 * <p>Esta classe inicia a aplicação cliente, apresenta um menu interativo para o utilizador,
 * e permite que ele faça o registo e o login no sistema. Após o login bem-sucedido, o cliente
 * inicia uma sessão de chat multicast.</p>
 */
@SpringBootApplication
public class Client {

    private static Connection connection;

    /**
     * Método principal que inicia o cliente e apresenta o menu de opções.
     *
     * <p>Este método cria uma conexão com o servidor e mostra um menu com opções para o utilizador.
     * O utilizador pode escolher entre se registrar, fazer login ou sair da aplicação.
     * Dependendo da escolha, o método chama o método apropriado para lidar com o registo ou login.</p>
     *
     * @param args Argumentos de linha de comando.
     * @throws IOException Se ocorrer algum erro ao se conectar ao servidor.
     */
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

    /**
     * Método para lidar com o registo de um novo utilizador.
     *
     * <p>Este método solicita ao utilizador o nome de utilizador, o email e a senha. O email é
     * validado com uma expressão regular. Após a entrada dos dados, o cliente envia uma solicitação
     * de registo para o servidor. Caso o registo seja bem-sucedido, o servidor envia uma resposta
     * que é mostrada ao utilizador.</p>
     *
     * @param scanner O objeto Scanner utilizado para ler a entrada do utilizador.
     */
    private static void handleSignUp(Scanner scanner) {
        connection.reconnect();
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

    /**
     * Método para lidar com o login de um utilizador.
     *
     * <p>Este método solicita ao utilizador o nome de utilizador ou email e a senha. A partir dessa
     * informação, o cliente envia uma solicitação de login para o servidor. Se o login for bem-sucedido,
     * o cliente irá tentar iniciar uma sessão de chat multicast. Caso contrário, será mostrada uma mensagem
     * de erro.</p>
     *
     * @param scanner O objeto Scanner utilizado para ler a entrada do utilizador.
     */
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
            int port = Integer.parseInt(matcher.group("port"));
            String name = matcher.group("name");

            try {
                MulticastChatService chatService = new MulticastChatService(groupAddress, port, name, connection.getSocket(), "localhost");
                chatService.startChat(groupAddress, port, name);
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
