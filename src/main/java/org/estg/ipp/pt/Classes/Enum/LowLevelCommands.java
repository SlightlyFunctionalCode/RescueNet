package org.estg.ipp.pt.Classes.Enum;

public enum LowLevelCommands {
    RESDIST("resdist"),;

    private final String description;

    LowLevelCommands(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
