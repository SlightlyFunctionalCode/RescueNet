package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.User;

public class ApprovalManager {

    public boolean approveOperation(User user, Operation operation){
        if(user.getPermissions().ordinal() >= operation.getRequiredPermission().ordinal()){
            System.out.println("Operação aprovada: " + operation.getName());
            return true;
        }else{
            System.out.println("Operação negada. Permissão insuficiente para: " + operation.getName());
            return false;
        }
    }
}
