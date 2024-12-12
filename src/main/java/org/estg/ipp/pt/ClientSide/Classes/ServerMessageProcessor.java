package org.estg.ipp.pt.ClientSide.Classes;

import org.estg.ipp.pt.ClientSide.Classes.Enums.ServerResponseRegex;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerMessageProcessor {
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

    private void sendIsReadConfirmation(String messageId, PrintWriter out) {
        String confirmationMessage = "CONFIRM_READ:" + messageId;
        out.println(confirmationMessage);
    }
}

