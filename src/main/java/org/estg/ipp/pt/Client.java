package org.estg.ipp.pt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {

    public static void main(String[] args) throws IOException {
        String serverAddress = "localhost";
        int serverPort = 5000;

        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nMENU");
                System.out.println("1. Registrar");
                System.out.println("2. Login");
                System.out.println("3. Logout");
                System.out.println("4. Sair");
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

                            if (isValidEmail(email)) {
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
                        System.out.print("Digite o nome de utilizar/email: ");
                        String usernameOremail = scanner.nextLine();
                        System.out.print("Digite a senha: ");
                        String password = scanner.nextLine();
                        out.println("LOGIN:" + usernameOremail + "," + password);
                        System.out.println(in.readLine());
                    }
                    case 3 -> {
                        System.out.print("Digite o nome de usuário: ");
                        String username = scanner.nextLine();
                        out.println("LOGOUT:" + username);
                        System.out.println(in.readLine());
                    }
                    case 4 -> {
                        System.out.println("Encerrando cliente...");
                        return;
                    }
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            }
        }
    }

    private static boolean isValidEmail(String email) {
        // Expressão regular para validar email
        String emailRegex = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}

