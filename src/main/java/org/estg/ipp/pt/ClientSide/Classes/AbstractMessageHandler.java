package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.Classes.Enum.RegexPatterns;
import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;

public abstract class AbstractMessageHandler implements MessageHandler {
    public AbstractMessageHandler() {}

    @Override
    public void handleMessage(String message) {
        if (RegexPatterns.DIRECTED_MESSAGE.matcher(message).matches()) {
            System.out.println("Mensagem direcionada a você: " + RegexPatterns.DIRECTED_MESSAGE.matcher(message).replaceFirst("$2"));
        } else if (RegexPatterns.NOTIFICATION.matcher(message).matches()) {
            System.out.println("⚠️ " + RegexPatterns.NOTIFICATION.matcher(message).replaceFirst("$1").trim());
        } else if (message.startsWith("CHAT_REQUEST")) {
            handleChatRequest(message);
        } else {
            System.out.println(message);
        }
    }

    protected abstract void handleChatRequest(String message);
}
