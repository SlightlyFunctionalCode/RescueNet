package org.estg.ipp.pt.Classes.Enum;

public enum MediumLevelCommands {
    EVAC("Evacuate");

    private final String description;

    MediumLevelCommands(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
