package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Classes.Enums.ServerResponseRegex;
import org.estg.ipp.pt.ClientSide.Interfaces.ChatService;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;

public class DefaultCommandHandler implements CommandHandler {
    private final String host;

    public DefaultCommandHandler(String host) {
        this.host = host;
    }

    @Override
    public void handleCommand(String command, String name, ChatService chatService) throws IOException {
        try (Socket serverSocket = new Socket(host, 5000);
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {
            out.println(command + ":" + name);

            String serverResponse = in.readLine();

            Matcher joinChatMatcher = ServerResponseRegex.SERVER_CHAT_GROUP.matcher(serverResponse);

            if (ServerResponseRegex.SERVER_PENDING.matches(serverResponse)) {
                System.out.println(Constants.SERVER_PENDING);
            } else if (ServerResponseRegex.SERVER_SUCCESS.matches(serverResponse)) {
                System.out.println(Constants.SERVER_SUCCESS);
            } else if (ServerResponseRegex.SERVER_ERROR.matcher(serverResponse).matches()) {
                System.out.println("Erro: " + ServerResponseRegex.SERVER_ERROR.matcher(serverResponse).replaceFirst("$1"));
            } else if (ServerResponseRegex.SERVER_APPROVE.matches(serverResponse)) {
                System.out.println(Constants.SERVER_APPROVE);
            } else if (ServerResponseRegex.SERVER_REJECT.matches(serverResponse)) {
                System.out.println(Constants.SERVER_REJECT);
            } else if (serverResponse.startsWith(Constants.SERVER_START_HELP)) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals(Constants.SERVER_END_HELP)) break;
                    System.out.println(line);
                }
            } else if (joinChatMatcher.matches()) {
                String newAddress = joinChatMatcher.group("address");
                String newPort = joinChatMatcher.group("port");
                try {
                    chatService.stopChat();
                    chatService = new MulticastChatService(newAddress, Integer.parseInt(newPort), name, serverSocket, host);
                    chatService.startChat(newAddress, Integer.parseInt(newPort), name);
                } catch (IOException e) {
                    out.println(Constants.ERROR_JOINING_CHAT);
                }
            } else {
                System.out.println(serverResponse);
            }
        } catch (NullPointerException | IOException e) {
            System.err.println(Constants.ERROR_SERVER_CONNECTION);
        }
    }
}
