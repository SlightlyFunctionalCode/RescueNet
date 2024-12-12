package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum {@code RegexPatterns} contém padrões de expressões regulares para validação de entradas
 * de texto em diferentes contextos de autenticação e ações do usuário, como registro, login,
 * confirmação de leitura, etc.
 *
 * Cada padrão corresponde a uma operação específica e pode ser usado para validar ou extrair
 * dados de uma string de entrada.
 *
 * <p>Os padrões de expressão regular disponíveis são:</p>
 * <ul>
 *     <li>{@code REGISTER} - Padrão para o registro de um usuário (username, email, password).</li>
 *     <li>{@code LOGIN} - Padrão para o login de um usuário (username, password).</li>
 *     <li>{@code READY} - Padrão para uma solicitação simples com o nome de usuário.</li>
 *     <li>{@code CONFIRM_READ} - Padrão para a confirmação de leitura (com id).</li>
 * </ul>
 */
public enum RegexPatterns {
    /**
     * Padrão para o registro de um usuário, que captura o nome de usuário, o email e a senha.
     * A entrada esperada é uma string com três partes separadas por vírgula.
     */
    REGISTER("^(?<username>.+),(?<email>.+),(?<password>.+)$"),

    /**
     * Padrão para o login de um usuário, que captura o nome de usuário e a senha.
     * A entrada esperada é uma string com duas partes separadas por vírgula.
     */
    LOGIN("^(?<username>.+),(?<password>.+)$"),

    /**
     * Padrão para uma solicitação de prontidão, que captura apenas o nome de usuário.
     * A entrada esperada é uma string com o nome de usuário.
     */
    READY("^(?<username>.+)$"),

    /**
     * Padrão para a confirmação de leitura, que captura um ID numérico.
     * A entrada esperada é uma string no formato "CONFIRM_READ:{id}", onde {id} é um número.
     */
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
     * Retorna um {@link Matcher} que pode ser usado para verificar se a entrada corresponde
     * ao padrão da expressão regular associada.
     *
     * @param input A string de entrada que será verificada contra a expressão regular.
     * @return O {@link Matcher} que pode ser utilizado para buscar correspondências na entrada.
     */
    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }
}
