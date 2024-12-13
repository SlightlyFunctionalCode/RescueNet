package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum {@code RegexPatterns}, contém padrões de expressões regulares para a validação de entradas
 * de texto em diferentes contextos de autenticação e ações do utilizador, como o registo, o login,
 * a confirmação do estado ready e a confirmação de leitura.
 *
 * <p>Cada padrão corresponde a uma operação específica e pode ser usado para validar ou extrair os
 * dados de uma ‘string’ de entrada.</p>
 *
 * <p>Os padrões de expressões regulares disponíveis são:</p>
 * <ul>
 *     <li>{@code REGISTER} - Padrão para o registo de um utilizador (username, email, password).</li>
 *     <li>{@code LOGIN} - Padrão para o login de um utilizador (username ou email, password).</li>
 *     <li>{@code READY} - Padrão para a confirmação do estado do utilizador, ou seja, se este
 *     já se encontra autenticado e pronto a receber mensagens.</li>
 *     <li>{@code CONFIRM_READ} - Padrão para a confirmação de leitura de mensagens privadas (com id).</li>
 * </ul>
 */
public enum RegexPatterns {

    REGISTER("^(?<username>.+),(?<email>.+),(?<password>.+)$"),
    LOGIN("^(?<username>.+),(?<password>.+)$"),
    READY("^(?<username>.+)$"),
    CONFIRM_READ("^CONFIRM_READ:(?<id>\\d+)$");

    private final Pattern pattern;

    /**
     * Construtor da enumeração {@code RegexPatterns}, que compila a expressão regular fornecida.
     *
     * @param regex A expressão regular a ser compilada e associada ao padrão.
     */
    RegexPatterns(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Devolve um {@link Matcher} que pode ser usado para verificar se a entrada corresponde
     * ao padrão da expressão regular associada.
     *
     * @param input A string de entrada que será verificada contra a expressão regular.
     * @return O {@link Matcher} que pode ser utilizado para buscar correspondências na entrada.
     */
    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }
}
