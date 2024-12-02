package org.estg.ipp.pt.Classes.Enum;

public enum HighLevelCommands {
    VIEW_REPORTS("View system reports"),
    MANAGE_USERS("Manage users"),
    CONFIGURE_SYSTEM("Configure system settings"),
    ACCESS_LOGS("Access system logs");
    private final String description;

    HighLevelCommands(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
