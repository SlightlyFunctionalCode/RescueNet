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
            - <id> id do pedido de Aprovação
            -
            - <username> username da pessoa que fez o request
            -
            - Exemplos:
            - /approve 1 user1
            --END HELP--
            """;

    public static final String REJECT_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /reject são:
            -
            - <id> id do pedido de Aprovação
            -
            - <username> username da pessoa que fez o request
            -
            - Exemplos:
            - /reject 1 user1
            --END HELP--
            """;

    public static final String CHANGE_PERMISSION_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /change_permission são:
            -
            - permissões disponiveis: (NO_LEVEL, LOW_LEVEL, MEDIUM_LEVEL, HIGH_LEVEL)
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
            - /chat user Hey, como estás?
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

    public static final String ADD_TO_GROUP_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /add_to_group são:
            -
            - <userToAdd> o nome do user que se pretende adicionar
            -
            - <group> nome do grupo
            -
            - Exemplos:
            - /add_to_group user1 MY_GROUP
            --END HELP--
            """;

    public static final String ALERT_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /alert são:
            -
            - <message> mensagem que se pretende enviar
            -
            - Exemplos:
            - /alert PERIGO PERIGO PERIGO
            --END HELP--
            """;

    public static final String COMMANDS_HIGH = """
            --HELP--
            - Comandos que podem ser executados no sistema são:
            -
            - /alert: permite alertar todos os utilizadores
            - /evac: permite criar pedido de evacução
            - /resdist: permite distribuir recursos de emergência
            - /emerg: permite ativar comunicações de emergência
            - /approve: permite aprovar um comando utilizado por outros utilizadores
            - /reject: permite rejeitar um comando utilizado por outros utilizadores
            - /join: permite entrar num grupo existente
            - /create_group: permite criar um novo grupo
            - /chat: permite enviar uma menssagem privada para um utilizador
            - /addToGroup: permite adicionar utilizadores a grupos privados se for owner do grupo
            - /groups: lista os grupos a que pertence
            - /leave: permite sair de um grupo para sempre
            - /export: permite exportar logs
            - /change_permission: permite alterar pemissões de utilizadores
            -
            --END HELP--
            """;

    public static final String COMMANDS_MEDIUM = """
            --HELP--
            - Comandos que podem ser executados no sistema são:
            -
            - /evac: permite criar pedido de evacução
            - /resdist: permite distribuir recursos de emergência
            - /emerg: permite ativar comunicações de emergência
            - /approve: permite aprovar um comando utilizado por outros utilizadores
            - /reject: permite rejeitar um comando utilizado por outros utilizadores
            - /join: permite entrar num grupo existente
            - /create_group: permite criar um novo grupo
            - /chat: permite enviar uma menssagem privada para um utilizador
            - /addToGroup: permite adicionar utilizadores a grupos privados se for owner do grupo
            - /groups: lista os grupos a que pertence
            - /leave: permite sair de um grupo para sempre
            --END HELP--
            """;

    public static final String COMMANDS_LOW = """
            --HELP--
            - Comandos que podem ser executados no sistema são:
            -
            - /resdist: permite distribuir recursos de emergência
            - /emerg: permite ativar comunicações de emergência
            - /approve: permite aprovar um comando utilizado por outros utilizadores
            - /reject: permite rejeitar um comando utilizado por outros utilizadores
            - /join: permite entrar num grupo existente
            - /create_group: permite criar um novo grupo
            - /chat: permite enviar uma menssagem privada para um utilizador
            - /addToGroup: permite adicionar utilizadores a grupos privados se for owner do grupo
            - /groups: lista os grupos a que pertence
            - /leave: permite sair de um grupo para sempre
            --END HELP--
            """;
    public static final String COMMANDS_DEFAULT = """
            --HELP--
            - Comandos que podem ser executados no sistema são:
            -
            - /resdist: permite distribuir recursos de emergência
            - /join: permite entrar num grupo existente
            - /create_group: permite criar um novo grupo
            - /chat: permite enviar uma menssagem privada para um utilizador
            - /addToGroup: permite adicionar utilizadores a grupos privados se for owner do grupo
            - /groups: lista os grupos a que pertence
            - /leave: permite sair de um grupo para sempre
            --END HELP--
            """;

    private HelpMessages() {
        throw new UnsupportedOperationException("Não se pode instanciar uma classe de constantes");
    }
}
