package org.estg.ipp.pt.Classes.Enum;

public enum Permissions {
    LOW_LEVEL,
    MEDIUM_LEVEL,
    HIGH_LEVEL,
    NO_LEVEL;


    // Método para converter um enum Permissions para o valor correspondente
    public static int fromPermissions(Permissions permissions) {
        return switch (permissions) {
            case LOW_LEVEL -> 0;
            case MEDIUM_LEVEL -> 1;
            case HIGH_LEVEL -> 2;
            default -> throw new IllegalArgumentException("Valor inválido para Permissions: " + permissions);
        };
    }

    // Método para converter um valor inteiro para o enum correspondente
    public static Permissions fromValue(int value) {
        return switch (value) {
            case 0 -> LOW_LEVEL;
            case 1 -> MEDIUM_LEVEL;
            case 2 -> HIGH_LEVEL;
            default -> throw new IllegalArgumentException("Valor inválido para Permissions: " + value);
        };
    }
}
