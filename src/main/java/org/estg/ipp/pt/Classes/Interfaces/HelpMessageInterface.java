package org.estg.ipp.pt.Classes.Interfaces;

public interface HelpMessageInterface {
    String EXPORT_HELP = """
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

    String APPROVE_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /approve são:
            -
            - <username> username da pessoa que fez o request
            -
            - Exemplos:
            - /approve pessoa1
            --END HELP--
            """;

    String REJECT_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /reject são:
            -
            - <username> username da pessoa que fez o request
            -
            - Exemplos:
            - /reject pessoa1
            --END HELP--
            """;

    String CHANGE_PERMISSION_HELP = """
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

    String JOIN_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /join são:
            -
            - <name> nome do grupo
            -
            - Exemplos:
            - /join LOW_LEVEL
            --END HELP--
            """;

    String CREATE_GROUP_HELP = """
            --HELP--
            - Os parâmetros que podem ser executados por /create_group são:
            -
            - <name> nome do grupo
            -
            - <address> endereço do grupo
            -
            - <port> porta do grupo
            -
            - Exemplos:
            - /create_group LOW_LEVEL
            --END HELP--
            """;
}
