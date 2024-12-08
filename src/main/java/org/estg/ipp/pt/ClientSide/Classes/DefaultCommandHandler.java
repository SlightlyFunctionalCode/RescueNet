package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ServerSide.Services.MulticastManagerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DefaultCommandHandler implements CommandHandler {
    private final String host;

    public DefaultCommandHandler(String host) {
        this.host = host;
    }

    @Override
    public void handleCommand(String command, String name, AbstractChatService multicastChatService) throws IOException {
        try (Socket serverSocket = new Socket(host, 5000);
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {
            out.println(command + ":" + name);

            String serverResponse = in.readLine();

            if (RegexPatterns.SERVER_PENDING.matches(serverResponse)) {
                System.out.println("Aguardando aprovação...");
            } else if (RegexPatterns.SERVER_SUCCESS.matches(serverResponse)) {
                System.out.println("Comando aprovado e executado.");
            } else if (RegexPatterns.SERVER_ERROR.matcher(serverResponse).matches()) {
                System.out.println("Erro: " + RegexPatterns.SERVER_ERROR.matcher(serverResponse).replaceFirst("$1"));
            } else if (RegexPatterns.SERVER_APPROVE.matches(serverResponse)) {
                System.out.println("Aprovado e executado.");
            } else if (RegexPatterns.SERVER_REJECT.matches(serverResponse)) {
                System.out.println("Rejeitado.");
            } else if (serverResponse.startsWith("--HELP--")) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("--END HELP--")) break;
                    System.out.println(line);
                }
            } else if (serverResponse.startsWith("CHAT_GROUP")) {
                String[] parts = serverResponse.split(":");
                String newAddress = parts[1];
                String newPort = parts[2];
                try {
                    multicastChatService.stopChat();
                    multicastChatService = new MulticastChatService(newAddress, Integer.parseInt(newPort), name, serverSocket, host);
                    multicastChatService.startChat(newAddress, Integer.parseInt(newPort), name);
                } catch (IOException e) {
                    out.println("ERRO: Falha ao tentar entrar no chat: " + e.getMessage());
                }
            } else {
                System.out.println(serverResponse);
            }
        } catch (NullPointerException | IOException e) {
            System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
        }
    }
}
