package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Enums.ServerResponseRegex;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A classe {@code ServerMessageProcessor} é responsável por processar as mensagens recebidas do servidor.
 * Ela valida, extrai e processa os dados das mensagens e pode enviar as confirmações de leitura para o servidor.
 *
 * <p>Quando uma mensagem privada é recebida, ela verifica se contém um ID de mensagem, remove certos padrões de formatação e
 * envia uma confirmação de leitura para o servidor.</p>
 */
public class ServerMessageProcessor {

    /**
     * Processa a mensagem recebida do servidor e caso esta seja uma mensagem privada, verifica se ela contém um ID de
     * mensagem e envia uma confirmação de leitura da mesma.
     *
     * @param message a mensagem recebida do servidor.
     * @param out o {@link PrintWriter} utilizado para enviar as mensagens de volta ao servidor.
     * @return a mensagem processada, sem o ID de mensagem e com a formatação corrigida.
     */
    public String processIncomingMessage(String message, PrintWriter out) {
        Matcher messageMatcher = ServerResponseRegex.MESSAGE.matcher(message);
        if (messageMatcher.matches()) {
            String messageId = messageMatcher.group("id");
            if (messageId != null) {
                messageId = messageId.replace("/", "");
                sendIsReadConfirmation(messageId, out);

                Pattern pattern = Pattern.compile("/.+?/");
                message = message.replaceAll(pattern.pattern(), "");
                return "**" + message + "**";
            }
        }
        return message;
    }

    /**
     * Envia uma confirmação de leitura para o servidor, a indicar que a mensagem foi lida.
     *
     * @param messageId o ID da mensagem que foi lida.
     * @param out o {@link PrintWriter} utilizado para enviar a confirmação ao servidor.
     */
    private void sendIsReadConfirmation(String messageId, PrintWriter out) {
        String confirmationMessage = "CONFIRM_READ:" + messageId;
        out.println(confirmationMessage);
    }
}

