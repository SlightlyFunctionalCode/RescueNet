package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.Permissions;

public class Operation {
    private final String name;
    private final Permissions requiredPermission;

    public Operation(String name, Permissions requiredPermission) {
        this.name = name;
        this.requiredPermission = requiredPermission;
    }

    public String getName() {
        return name;
    }

    public Permissions getRequiredPermission() {
        return requiredPermission;
    }
}
