package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A interface {@code ExecuteInternalCommands} define o contrato para a execução
 * de comandos internos no servidor.
 *
 * <p><b>Funcionalidades:</b></p>
 * <ul>
 *   <li>Verificação de comandos internos.</li>
 *   <li>Manipulação de comandos internos com base no contexto do cliente e do servidor.</li>
 *   <li>Fornecimento de acesso ao processador de comandos internos.</li>
 * </ul>
 *
 * <p>Esta interface é implementada por classes que executam comandos internos do servidor.</p>
 *
 * @see Permissions
 * @see Group
 * @see ProcessInternalCommands
 */
public interface ExecuteInternalCommands {
    /**
     * Verifica se um comando fornecido é um comando interno reconhecido pelo sistema.
     *
     * @param command O comando a ser verificado.
     * @return {@code true} se o comando for interno; caso contrário, {@code false}.
     */
    boolean isInternalCommand(String command);

    /**
     * Lida com um comando interno específico e processa a sua lógica com base nos parâmetros fornecidos.
     *
     * @param command O comando interno a ser executado.
     * @param payload Os dados ou argumentos associados ao comando.
     * @param out O objeto {@link PrintWriter} usado para enviar respostas ao cliente.
     * @param clientSocket O {@link Socket} do cliente que enviou o comando.
     * @param groupList A lista de grupos disponíveis no servidor.
     * @param usersWithPermissionsOnline Um mapa de utilizadores online com as suas respetivas permissões.
     * @throws IllegalArgumentException Se algum dos parâmetros obrigatórios for nulo.
     */
    void handleInternalCommand(String command, String payload, PrintWriter out,
                               Socket clientSocket, List<Group> groupList, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline);
}
