package org.estg.ipp.pt.ClientSide.Classes.Constants;

public final class Constants {
    public static final String MENU = """
            ============================
                        MENU
            1. Registar
            2. Login
            3. Sair
            ============================
            Escolha uma opção:\s""";
    public static final String EXITING_APP = "Encerrando o cliente...";
    public static final String INPUT_USER_NAME = "Digite o seu nome de utilizador: ";
    public static final String INPUT_USER_EMAIL = "Digite o seu email: ";
    public static final String INPUT_USER_PASSWORD = "Digite o seu senha: ";
    public static final String INPUT_USER_NAME_EMAIL = "Digite o seu nome/email: ";

    public static final String ERROR_GENERIC = "ERRO: Ocorreu um erro";
    public static final String ERROR_INVALID_CREDENTIALS = "Falha ao iniciar sessão. Verifique suas credenciais.";
    public static final String ERROR_LOGIN = "Ocorreu um erro ao efetuar o Login";
    public static final String ERROR_SIGN_UP = "Ocorreu um erro ao efetuar o Registo";
    public static final String ERROR_INVALID_EMAIL = "Email inválido. Certifique-se de que contém '@' e '.' após o '@'. Tente novamente.";
    public static final String ERROR_INVALID_MENU_OPTION = "Opção inválida. Tente novamente.";
    public static final String ERROR_STARTING_CHAT_SESSION = "Erro: Ocorreu um erro ao iniciar o chat";
    public static final String ERROR_CHAT_SESSION = "Erro: Ocorreu um erro no decorrer da sessão de chat";
    public static final String ERROR_RECEIVING_MESSAGE = "Erro: Ocorreu um erro ao receber uma mensagem";
    public static final String ERROR_SENDING_MESSAGE = "Erro: Ocorreu um erro ao mandar uma mensagem";
    public static final String ERROR_JOINING_CHAT = "ERRO: Falha ao tentar entrar no chat";
    public static final String ERROR_SERVER_CONNECTION = "Erro: Ocorreu um erro ao comunicar com o servidor";
    public static final String ERROR_LEAVING_CHAT = "Erro: Ocorreu um erro ao sair do chat multicast";

    // Respostas de Comandos
    public static final String SERVER_PENDING = "Aguardando aprovação...";
    public static final String SERVER_SUCCESS = "Comando aprovado e executado.";
    public static final String SERVER_APPROVE = "Aprovado e executado.";
    public static final String SERVER_REJECT = "Rejeitado.";
    public static final String SERVER_START_HELP = "--HELP--";
    public static final String SERVER_END_HELP = "--END HELP--";

    private Constants() {
        throw new UnsupportedOperationException("Não se pode instanciar uma classe de constantes");
    }
}
