package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Constants.Constants;
import org.estg.ipp.pt.ClientSide.Interfaces.ChatService;
import org.estg.ipp.pt.ClientSide.Interfaces.CommandHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.InputHandler;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;

import java.io.IOException;
import java.util.Scanner;

/**
 * A classe {@code UserInputHandler} é responsável por gerir a entrada de mensagens do utilizador
 * durante uma sessão de chat. Ela processa comandos específicos e envia mensagens para o servidor
 * ou para o serviço de chat conforme o utilizador interage com o sistema.
 *
 * <p>Ela permite ao utilizador enviar mensagens normais, executar comandos e se desconectar do chat
 * através de comandos especiais como "/logout".</p>
 */
public class UserInputHandler implements InputHandler {
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final ChatService chatService;

    /**
     * Constrói um manipulador de entrada do utilizador com os manipuladores de comandos, mensagens e o serviço de chat.
     *
     * @param commandHandler o manipulador de comandos para processar comandos de chat.
     * @param messageHandler o manipulador de mensagens para enviar mensagens do utilizador.
     * @param chatService o serviço de chat responsável por gerir a comunicação.
     */
    public UserInputHandler(CommandHandler commandHandler, MessageHandler messageHandler, ChatService chatService) {
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
        this.chatService = chatService;
    }

    /**
     * Inicia o processo de captura de entrada do utilizador e envia comandos ou mensagens conforme a entrada.
     *
     * <p>O método monitora a entrada do utilizador, permitindo o envio de mensagens para o chat ou a execução
     * de comandos. Caso o comando "/logout" seja digitado, a sessão de chat é encerrada. Caso contrário,
     * ele envia mensagens normais para o servidor.</p>
     *
     * @param name o nome do utilizador, incluído em todas as mensagens enviadas.
     */
    public void handleInput(String name) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite a mensagem (/logout para sair): ");
        System.out.println("Digite /commands para visualizar os comandos disponíveis:");
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
