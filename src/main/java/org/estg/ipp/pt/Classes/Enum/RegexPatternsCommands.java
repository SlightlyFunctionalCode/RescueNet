package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum {@code RegexPatternsCommands} que contém os padrões de expressões regulares para validar e
 * processar comandos de texto específicos relacionados a ações no sistema, como as aprovações,
 * as rejeições, as exportações, a criação de grupos, entre outros.
 *
 * <p>Cada comando tem um padrão específico que pode ser utilizado para validar ou extrair os
 * parâmetros de comandos fornecidos em formato de ‘string’.</p>
 *
 * <p>Os comandos disponíveis incluem:</p>
 * <ul>
 *     <li>{@code APPROVE} - Comando para aprovar uma solicitação com um ID e o solicitante.</li>
 *     <li>{@code REJECT} - Comando para rejeitar uma solicitação com um ID e o solicitante.</li>
 *     <li>{@code EXPORT} - Comando para exportar os logs do sistema num intervalo de datas e/ou com uma tag específica.</li>
 *     <li>{@code REQUEST} - Comando 'default' para a solicitação que contém o payload e solicitante.</li>
 *     <li>{@code JOIN} - Comando para um utilizador se juntar a um grupo.</li>
 *     <li>{@code CHANGE_PERMISSIONS} - Comando para alterar as permissões de um utilizador.</li>
 *     <li>{@code CREATE_GROUP} - Comando para criar um grupo, público ou privado.</li>
 *     <li>{@code CHAT} - Comando para enviar uma mensagem de chat para outro utilizador.</li>
 *     <li>{@code COMMANDS} - Comando para mostrar todos os comandos disponíveis ao utilizador.</li>
 *     <li>{@code ADD_TO_GROUP} - Comando para adicionar um utilizador a um grupo.</li>
 *     <li>{@code LIST_GROUPS} - Comando para mostrar todos os grupos de um utilizador.</li>
 *     <li>{@code LEAVE_GROUP} - Comando para sair de um grupo.</li>
 *     <li>{@code ALERT} - Comando para enviar um alerta a todos os grupos.</li>
 *     <li>{@code LOGOUT} - Comando para fazer o logout de um utilizador.</li>
 * </ul>
 */
public enum RegexPatternsCommands {
    APPROVE("^/approve(?:\\s(?<help>-h))?(?:\\s(?<id>\\d+)\\s(?<requester>[^\\s:]+))?:(?<username>.+)$"),
    REJECT("^/reject(?:\\s(?<help>-h))?(?:\\s(?<id>\\d+)\\s(?<requester>[^\\s:]+))?:(?<username>.+)$"),
    EXPORT("^/export(?:\\s(?<help>-h))?(?:\\s(?<startDate>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}))?(?:\\s(?<endDate>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}))?(?:\\s(?<tag>[^\\s:]+))?:(?<username>.+)$"),
    REQUEST("^(?<command>/\\w+|\\w+)(?:\\s+(?<requester>(?:[^\\s:]+(?:\\s[^\\s:]+)*)))?:(?<payload>.*)$"),
    JOIN("^/join(?:\\s(?<help>-h))?(?:\\s(?<name>.+))?:(?<requester>.+)$"),
    CHANGE_PERMISSIONS("^/change_permission(?:\\s(?<help>-h))?(?:\\s(?<name>.+)\\s(?<permission>.+))?:(?<requester>.+)$"),
    CREATE_GROUP("^/create_group(?:\\s(?<help>-h))?(?:\\s(?<name>.+)\\s(?<publicOrPrivate>.+))?:(?<requester>.+)$"),
    CHAT("^/chat(?:\\s(?<help>-h))?(?:\\s(?<targetUsername>\\S+)\\s(?<message>.+))?:(?<username>.+)$"),
    COMMANDS("^/commands:(?<name>.+)$"),
    ADD_TO_GROUP("^/add_to_group(?:\\s(?<help>-h))?(?:\\s(?<userToAdd>.+)\\s(?<group>[^\\s:]+))?:(?<username>.+)"),
    LIST_GROUPS("^/groups:(?<username>.+)$"),
    LEAVE_GROUP("^/leave(?:\\s(?<help>-h))?(?:\\s(?<groupName>\\S+))?:(?<username>.+)$"),
    ALERT("^/alert(?:\\s(?<help>-h))?(?:\\s+(?<message>.+))?:(?<username>\\S+)$"),
    LOGOUT("^/logout:(?<username>\\S+)$");


    private final Pattern pattern;

    /**
     * Construtor da enumeração {@code RegexPatternsCommands}, que compila a expressão regular fornecida para cada comando.
     *
     * @param regex A expressão regular a ser compilada e associada ao padrão do comando.
     */
    RegexPatternsCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Devolve um {@link Matcher} que pode ser usado para verificar se a entrada corresponde
     * ao padrão da expressão regular associada ao comando.
     *
     * @param input A string de entrada que será verificada contra a expressão regular.
     * @return O {@link Matcher} que pode ser utilizado para encontrar ou verificar as correspondências da entrada.
     */
    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }
}
