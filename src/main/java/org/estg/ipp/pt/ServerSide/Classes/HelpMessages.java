package org.estg.ipp.pt.ServerSide.Classes;

public final class HelpMessages {
    public static final String EXPORT_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /export são:
            -
            - <startDate> data e hora inicial (formato YYYY-MM-DDThh:mm:ss)
            -
            - <endDate> data e hora final (formato YYYY-MM-DDThh:mm:ss)
            -
            - <tag> tag do tipo TagType
            -
            - Exemplos:
            - /export INFO
            - /export 2024-01-01T00:00:00 2024-01-01T00:00:00
            - /export 2024-01-01T00:00:00 2024-01-01T00:00:00 ERROR
            --END HELP--
            """;

    public static final String APPROVE_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /approve são:
            -
            - <username> username da pessoa que fez o request
            -
            - Exemplos:
            - /approve pessoa1
            --END HELP--
            """;

    public static final String REJECT_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /reject são:
            -
            - <username> username da pessoa que fez o request
            -
            - Exemplos:
            - /reject pessoa1
            --END HELP--
            """;

    public static final String CHANGE_PERMISSION_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /change_permission são:
            -
            - <username> username da pessoa que se pretende mudar as permissões
            -
            - <permission> novas permissões (número da permissão)
            -
            - Exemplos:
            - /change_permission pessoa 1
            --END HELP--
            """;

    public static final String JOIN_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /join são:
            -
            - <name> nome do grupo
            -
            - Exemplos:
            - /join LOW_LEVEL
            --END HELP--
            """;

    public static final String CREATE_GROUP_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /create_group são:
            -
            - <name> nome do grupo
            -
            - <privacy> 'public' ou 'private'
            -
            - Exemplos:
            - /create_group example public
            --END HELP--
            """;

    public static final String CHAT_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /chat são:
            -
            - <targetUsername> nome do destinatário
            -
            - <message> mensagem que se pretende enviar
            -
            - Exemplos:
            - /chat user hey como estás?
            --END HELP--
            """;

    public static final String LEAVE_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /leave são:
            -
            - <groupName> nome do grupo
            -
            - Exemplos:
            - /leave GROUP
            --END HELP--
            """;

    private HelpMessages() {
        throw new UnsupportedOperationException("Não se pode instanciar uma classe de constantes");
    }
}
