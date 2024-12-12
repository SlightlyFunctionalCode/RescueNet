package org.estg.ipp.pt.ClientSide.Interfaces;

import java.io.IOException;

/**
 * A interface {@code CommandHandler} define o contrato para classes responsáveis por
 * processar e executar comandos recebidos no contexto de um chat.
 *
 * <p>Implementações dessa interface devem ser capazes de interpretar e reagir a comandos
 * específicos, como comandos de chat (/logout, /commands, etc.), e executar as ações associadas.</p>
 */
public interface CommandHandler {

    /**
     * Processa e executa um comando recebido.
     *
     * <p>Este método é responsável por processar um comando fornecido pelo utilizador no chat,
     * realizando a ação associada ao comando. Isso pode incluir, por exemplo, sair do chat
     * ou visualizar a lista de comandos disponíveis.</p>
     *
     * @param command o comando a ser processado (exemplo: "/logout", "/commands").
     * @param name o nome do utilizador que está a enviar o comando.
     * @param chatService o serviço de chat que pode ser utilizado para executar ações no chat.
     */
    void handleCommand(String command, String name, ChatService chatService);
}
