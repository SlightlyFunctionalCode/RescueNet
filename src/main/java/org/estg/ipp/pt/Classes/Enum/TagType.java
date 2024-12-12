package org.estg.ipp.pt.Classes.Enum;

/**
 * Enum {@code TagType} representa diferentes tipos de tags para categorizar logs de eventos, com cada tipo
 * associado a uma descrição que explica o seu propósito.
 *
 * <p>Os tipos de tags disponíveis incluem:</p>
 * <ul>
 *     <li>{@code INFO} - Mensagens informativas gerais.</li>
 *     <li>{@code ERROR} - Erros que requerem atenção.</li>
 *     <li>{@code ALERT} - Alertas críticos que podem indicar um problema grave.</li>
 *     <li>{@code CRITICAL} - Problemas graves que exigem atenção imediata.</li>
 *     <li>{@code SUCCESS} - Indica operações bem-sucedidas.</li>
 *     <li>{@code FAILURE} - Indica operações malsucedidas.</li>
 *     <li>{@code ACCESS} - Logs relacionados ao controle de acesso ou tentativas.</li>
 *     <li>{@code USER_ACTION} - Logs de ações ou entradas do utilizador.</li>
 *     <li>{@code DATABASE} - Logs relacionados a operações de base de dados.</li>
 *     <li>{@code NETWORK} - Logs relacionados à rede (e.g., problemas de conexão).</li>
 *     <li>{@code SECURITY} - Logs relacionados à segurança (e.g., acesso não autorizado).</li>
 * </ul>
 */
public enum TagType {
    INFO("General informational messages."),
    ERROR("Errors that require attention."),
    ALERT("Critical alerts that might indicate a major issue."),
    CRITICAL("Severe issues requiring immediate attention."),
    SUCCESS("Indicates successful operations."),
    FAILURE("Indicates unsuccessful operations."),
    ACCESS("Logs related to access control or attempts."),
    USER_ACTION("Logs of user actions or inputs."),
    DATABASE("Logs related to database operations."),
    NETWORK("Network-related logs (e.g., connection issues)."),
    SECURITY("Security-related logs (e.g., unauthorized access).");

    private final String description;

    /**
     * Construtor da enumeração {@code TagType}, que associa uma descrição a cada tipo de tag.
     *
     * @param description A descrição do tipo de tag.
     */
    TagType(String description) {
        this.description = description;
    }

    /**
     * Retorna a descrição do tipo de tag.
     *
     * @return A descrição associada ao tipo de tag.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retorna o nome da tag como uma string.
     *
     * @return O nome da tag como string.
     */
    @Override
    public String toString() {
        return name();
    }
}
