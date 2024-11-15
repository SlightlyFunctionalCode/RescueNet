package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.User;

public class AuthorizationService {
    public boolean canApprove(User user, String operationType) {
        switch (operationType) {
            case "Evacuação":
                return user.getProfile().equals("Coordenador Regional");
            case "Comunicações de Emergência":
                return user.getProfile().equals("Nível Médio");
            case "Distribuição de Recursos":
                return user.getProfile().equals("Nível Baixo");
            default:
                return false;
        }
    }
}
