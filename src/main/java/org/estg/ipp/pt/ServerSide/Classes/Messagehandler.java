package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.ClientSide.Interfaces.MessageHandler;

public class Messagehandler implements MessageHandler {
    public void handleMessage(String message) {
        System.out.println(message);
    }
}
