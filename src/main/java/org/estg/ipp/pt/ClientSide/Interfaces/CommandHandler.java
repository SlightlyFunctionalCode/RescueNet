package org.estg.ipp.pt.ClientSide.Interfaces;

/**
 * A interface {@code CommandHandler} define o contrato para as classes responsáveis por
 * processar as respostas do servidor a comandos introduzidos pelo utilizador.
 *
 * <p>As implementações desta interface devem ser capazes de processar as respostas do servidor.</p>
 */
public interface CommandHandler {

    /**
     * Processa e executa as ações necessárias em resposta à mensagem do servidor.
     *
     * @param command o comando a ser processado.
     * @param name o nome do utilizador que está a enviar o comando.
     * @param chatService o serviço de chat que pode ser utilizado para executar as ações no chat.
     */
    void handleCommand(String command, String name, ChatService chatService);
}
