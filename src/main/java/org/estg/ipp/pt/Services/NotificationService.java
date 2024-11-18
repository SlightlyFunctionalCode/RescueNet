package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;

public class NotificationService {

    public void sendNotification(String message, Permissions targetGroup){
        System.out.println("Enviando notificação para " + targetGroup + ": " + message);
    }
}
