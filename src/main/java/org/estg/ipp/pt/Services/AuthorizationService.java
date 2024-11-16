package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.User;

public class AuthorizationService {
    public boolean canApprove(User user, String operationType) {
        switch (operationType) {
            case "Evacuação":
                return user.getPermissions().equals(Permissions.HIGH_LEVEL);
            case "Comunicações de Emergência":
                return user.getPermissions().equals(Permissions.MEDIUM_LEVEL);
            case "Distribuição de Recursos":
                return user.getPermissions().equals(Permissions.LOW_LEVEL);
            default:
                return false;
        }
    }
}
