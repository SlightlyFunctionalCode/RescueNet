package org.estg.ipp.pt.ClientSide.Classes.Enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A enumeração {@code ServerResponseRegex} define os padrões regex utilizados para interpretar
 * e validar as respostas do servidor na aplicação client-side.
 *
 * <p>Cada constante enum representa um padrão regex associado a uma resposta específica ou
 * a um formato esperado de mensagem. A enum fornece os métodos para verificar se uma ‘string’
 * corresponde ao padrão e para criar um {@link Matcher} a partir da ‘string’ fornecida.</p>
 */
public enum ServerResponseRegex {
    SERVER_PENDING("^PENDENTE$"),
    SERVER_SUCCESS("^SUCESSO$"),
    SERVER_ERROR("^ERRO:(?<errorMessage>.*)$"),
    SERVER_APPROVE("^APPROVE$"),
    SERVER_REJECT("^REJECT$"),
    LOGIN_SUCCESS("^SUCESSO:.*Grupo:\\s(?<address>[\\d\\.]+):(?<port>\\d+):(?<name>.+)$"),
    LOGIN_FAILED("^FAILED$"),
    GENERIC_RESPONSE("^(?<status>SUCESSO|FAILED|ERROR):.*$"),
    EMAIL("^(?<email>[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,})$"),
    MESSAGE("^PRIVATE:(?<id>/\\d+/).+$"),
    SERVER_CHAT_GROUP("^CHAT_GROUP:(?<address>.+):(?<port>.+)");

    private final Pattern pattern;

    /**
     * Construtor da enumeração.
     *
     * @param regex o padrão regex associado à constante enum.
     */
    ServerResponseRegex(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Cria um {@link Matcher} para o ‘input’ fornecido com base no padrão regex desta constante.
     *
     * @param input a ‘string’ a ser comparada com o padrão.
     * @return um {@code Matcher} para verificar as correspondências ou extrair os grupos.
     */
    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }

    /**
     * Verifica se a ‘string’ fornecida corresponde ao padrão regex desta constante.
     *
     * @param input a string a ser validada.
     * @return {@code true} se a ‘string’ corresponde ao padrão, {@code false} caso contrário.
     */
    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
