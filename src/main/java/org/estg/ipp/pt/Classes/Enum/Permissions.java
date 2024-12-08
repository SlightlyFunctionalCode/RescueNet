package org.estg.ipp.pt.Classes.Enum;

public enum Permissions {
    NO_LEVEL,
    LOW_LEVEL,
    MEDIUM_LEVEL,
    HIGH_LEVEL;


    // Método para converter um enum Permissions para o valor correspondente
    public static int fromPermissions(Permissions permissions) {
        return switch (permissions) {
            case NO_LEVEL -> 0;
            case LOW_LEVEL -> 1;
            case MEDIUM_LEVEL -> 2;
            case HIGH_LEVEL -> 3;
            default -> throw new IllegalArgumentException("Valor inválido para Permissions: " + permissions);
        };
    }

    // Método para converter um valor inteiro para o enum correspondente
    public static Permissions fromValue(int value) {
        return switch (value) {
            case 0 -> NO_LEVEL;
            case 1 -> LOW_LEVEL;
            case 2 -> MEDIUM_LEVEL;
            case 3 -> HIGH_LEVEL;
            default -> throw new IllegalArgumentException("Valor inválido para Permissions: " + value);
        };
    }
}
