package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A interface {@code ExecuteUserCommands} define o contrato para a manipulação de comandos enviados pelos utilizadores.
 *
 * <p><b>Funcionalidades:</b></p>
 * <ul>
 *   <li>Processamento e execução de comandos enviados pelos utilizadores.</li>
 * </ul>
 *
 * <p>Esta interface é implementada por classes responsáveis por interpretar e executar os comandos
 * originados de clientes conectados ao servidor.</p>
 *
 * @see Permissions
 */
public interface ExecuteUserCommands {

    /**
     * Executa um comando enviado por um utilizador.
     *
     * <p>Este método processa o comando com base nos parâmetros fornecidos, a partir dos detalhes do utilizador,
     * da solicitação associada e quaisquer permissões que possam ser relevantes.</p>
     *
     * @param command O comando enviado pelo utilizador.
     * @param request A solicitação original associada ao comando.
     * @param requester O nome ou identificador do utilizador que enviou o comando.
     * @param payload Os dados adicionais fornecidos pelo utilizador junto ao comando.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     * @param usersWithPermissionsOnline Um mapa que contém os utilizadores atualmente online e as suas permissões associadas.
     * @throws IOException Se ocorrer um erro durante o processamento ou envio de dados.
     * @throws IllegalArgumentException Se algum dos parâmetros obrigatórios for nulo ou inválido.
     */
    void handleUserCommand(String command, String request, String requester, String payload, PrintWriter out,
                           ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) throws IOException;
}
