package org.estg.ipp.pt.Classes.Enum;

/**
 * Enum {@code Permissions} representa diferentes níveis de permissões de acesso em um sistema.
 * Cada nível de permissão é mapeado para um valor inteiro, e os métodos utilitários permitem a conversão entre
 * os valores inteiros e os valores da enumeração.
 *
 * <p>Os níveis de permissão disponíveis são:</p>
 * <ul>
 *     <li>{@code NO_LEVEL} - Nenhum nível de permissão.</li>
 *     <li>{@code LOW_LEVEL} - Nível baixo de permissão.</li>
 *     <li>{@code MEDIUM_LEVEL} - Nível médio de permissão.</li>
 *     <li>{@code HIGH_LEVEL} - Nível alto de permissão.</li>
 * </ul>
 */
public enum Permissions {
    /**
     * Nenhum nível de permissão.
     */
    NO_LEVEL,

    /**
     * Nível baixo de permissão.
     */
    LOW_LEVEL,

    /**
     * Nível médio de permissão.
     */
    MEDIUM_LEVEL,

    /**
     * Nível alto de permissão.
     */
    HIGH_LEVEL;


    /**
     * Converte um valor do tipo {@link Permissions} para o valor inteiro correspondente.
     *
     * @param permissions O nível de permissão a ser convertido.
     * @return O valor inteiro correspondente ao nível de permissão.
     */
    public static int fromPermissions(Permissions permissions) {
        return switch (permissions) {
            case NO_LEVEL -> 0;
            case LOW_LEVEL -> 1;
            case MEDIUM_LEVEL -> 2;
            case HIGH_LEVEL -> 3;
        };
    }

    /**
     * Converte um valor inteiro para o nível de permissão correspondente.
     *
     * @param value O valor inteiro a ser convertido para o nível de permissão.
     * @return O nível de permissão correspondente ao valor fornecido.
     * @throws IllegalArgumentException Se o valor fornecido não corresponder a um nível de permissão válido.
     */
    public static Permissions fromValue(int value) throws IllegalArgumentException {
        return switch (value) {
            case 0 -> NO_LEVEL;
            case 1 -> LOW_LEVEL;
            case 2 -> MEDIUM_LEVEL;
            case 3 -> HIGH_LEVEL;
            default -> throw new IllegalArgumentException("Valor inválido para Permissions: " + value);
        };
    }
}
