package org.estg.ipp.pt.Classes.Enum;

public enum NoLevelCommands {
    COMM("comm");

    private final String description;

    NoLevelCommands(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
