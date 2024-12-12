package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Interfaces.ChatService;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.InputHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;

import java.io.IOException;
import java.util.Scanner;

public class UserInputHandler implements InputHandler {
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final ChatService chatService;

    public UserInputHandler(CommandHandler commandHandler, MessageHandler messageHandler, ChatService chatService) {
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
        this.chatService = chatService;
    }

    public void handleInput(String name) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite a mensagem (/logout para sair): ");
        boolean shouldExit = false;

        while (!shouldExit) {
            String msg = scanner.nextLine();
            if (msg.equalsIgnoreCase("/logout")) {
                shouldExit = true;
                commandHandler.handleCommand("/logout", name, chatService);
            } else if (msg.startsWith("/")) {
                    commandHandler.handleCommand(msg, name, chatService);
            } else {
                try {
                    messageHandler.sendMessage(name + ":" + msg);
                } catch (IOException e) {
                    System.out.println(Constants.ERROR_SENDING_MESSAGE);
                }
            }
        }
    }
}
